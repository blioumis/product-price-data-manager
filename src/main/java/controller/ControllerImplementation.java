package controller;

import java.io.IOException;
import java.util.List;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;

import dto.CategoryHighlightDTO;
import dto.MeasurementDTO;
import dto.ProductDTO;
import dto.ProductHighlightDTO;
import dto.ProductStatsDTO;
import dto.Top10AppearanceDTO;
import dto.YearDTO;

/**
 * Implements the business logic defined by the IController interface.
 * Handles data loading, filtering, statistical calculations and reports.
 */
public class ControllerImplementation implements IController {

    // Product metadata and alias mappings.
    private final HashMap<String,String> aliasToTitle = new HashMap<>();
    private final HashMap<String,String> aliasToCategory = new HashMap<>();
    private final HashMap<String,String> titleToAlias = new HashMap<>();

    // Product prices, Top-10 appearances and headlines grouped by year.
    private final HashMap<Integer,HashMap<String,Double>> pricesbyyear = new HashMap<>();
    private final HashMap<Integer,ArrayList<String>> top10byyear = new HashMap<>();
    private final HashMap<Integer,ArrayList<String>> headlinesbyyear = new HashMap<>();

    // Preserves the original product column order.
    private final ArrayList<String> aliasorder = new ArrayList<>();

    // Maps normalized product names and aliases to their original aliases.
    private final HashMap<String,String> keyToAlias = new HashMap<>();


    /**
     * Reads an INI configuration file and returns its key-value pairs.
     * Empty lines, comments and section headers are ignored.
     */
    private HashMap<String,String> readConfigIniPaths(String path) throws IOException {

        HashMap<String,String> m = new HashMap<>();
        BufferedReader br = null;

        try {
            br = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8);
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")
                        || line.startsWith(";") || line.startsWith("[")) {
                    continue;
                }

                // Split only at the first equals sign.
                String[] parts = line.split("=", 2);

                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    m.put(key, value);
                }
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return m;
    }


    /**
     * Loads product metadata and initializes the alias, title
     * and category mappings.
     */
    private void load_metadatatsv(String metapath) throws IOException {

        // Remove previously loaded metadata.
        this.aliasToTitle.clear();
        this.aliasToCategory.clear();
        this.titleToAlias.clear();
        this.keyToAlias.clear();

        BufferedReader br = null;

        try {
            br = Files.newBufferedReader(Paths.get(metapath), StandardCharsets.UTF_8);
            String line;

            while ((line = br.readLine()) != null) {

                String[] c = line.split("\t", -1);

                // Ignore rows that do not contain all required fields.
                if (c.length < 3) {
                    continue;
                }

                String title = c[0].trim();
                String alias = c[1].trim();
                String category = c[2].trim();

                if (alias.length() == 0) {
                    continue;
                }

                this.aliasToTitle.put(alias, title);
                this.aliasToCategory.put(alias, category);

                // Store alternative lookup keys for product names.
                if (title.length() > 0) {
                    this.titleToAlias.put(title.toLowerCase(Locale.ROOT), alias);
                    this.keyToAlias.put(replace(title), alias);
                }

                this.keyToAlias.put(replace(alias), alias);
            }

        } finally {
            if (br != null) {
                br.close();
            }
        }
    }


    /**
     * Loads yearly product prices, Top-10 appearances and headlines.
     * Returns the number of distinct years loaded into memory.
     */
    private int load_datatsv(String datapath, String delimiter) throws IOException {

        // Remove previously loaded measurements and reports.
        this.pricesbyyear.clear();
        this.top10byyear.clear();
        this.headlinesbyyear.clear();
        this.aliasorder.clear();

        BufferedReader br = null;

        try {
            br = Files.newBufferedReader(Paths.get(datapath), StandardCharsets.UTF_8);

            String header = br.readLine();

            if (header == null) {
                return 0;
            }

            String[] columns = splitDataLine(header, delimiter);
            String[] colAliases = new String[columns.length];

            // Associate each product column with its corresponding alias.
            for (int i = 1; i < columns.length; i++) {

                String title = columns[i].trim();

                // These columns contain reports rather than product prices.
                if (title.equals("CommodityTop10") || title.equals("News Headline")) {
                    colAliases[i] = null;
                    continue;
                }

                String alias = findAlias(title);

                colAliases[i] = alias;

                if (!this.aliasorder.contains(alias)) {
                    this.aliasorder.add(alias);
                }
            }

            String line;

            while ((line = br.readLine()) != null) {

                String[] c = splitDataLine(line, delimiter);

                if (c.length == 0) {
                    continue;
                }

                int year;

                // Skip rows containing an invalid year.
                try {
                    year = Integer.parseInt(c[0].trim());
                } catch (Exception e) {
                    continue;
                }

                // Create a price map for the year if one does not exist.
                HashMap<String,Double> row = this.pricesbyyear.get(year);

                if (row == null) {
                    row = new HashMap<>();
                    this.pricesbyyear.put(year, row);
                }

                ArrayList<String> yeartop10 = new ArrayList<>();
                ArrayList<String> yearheadlines = new ArrayList<>();

                for (int i = 1; i < columns.length; i++) {

                    if (i >= c.length) {
                        continue;
                    }

                    String title = columns[i].trim();
                    String valueText = c[i].trim();

                    if (valueText.length() == 0) {
                        continue;
                    }

                    // Parse the Top-10 product list for the current year.
                    if (title.equals("CommodityTop10")) {

                        String listText = valueText.replace(';', ',');
                        String[] parts = listText.split(",", -1);

                        for (String p : parts) {

                            // Remove surrounding quotation marks.
                            String t = p.trim().replaceAll("^\"+|\"+$", "").trim();

                            if (t.length() == 0) {
                                continue;
                            }

                            String sh = findAlias(t);
                            yeartop10.add(sh);
                        }

                        continue;
                    }

                    // Headlines are separated by the pipe character.
                    if (title.equals("News Headline")) {

                        String[] blocks = valueText.split("\\|", -1);

                        for (String p : blocks) {

                            String h = p.trim();

                            // Preserve empty headline positions.
                            if (!h.isEmpty()) {
                                yearheadlines.add(h);
                            } else {
                                yearheadlines.add("");
                            }
                        }

                        continue;
                    }

                    String alias = colAliases[i];

                    if (alias == null) {
                        continue;
                    }

                    // Missing measurements are excluded from the price map.
                    if (valueText.equals("NA") || valueText.equals("na")
                            || valueText.equals("Na") || valueText.equals("nA")) {
                        continue;
                    }

                    // Store valid numerical measurements.
                    try {
                        double value = Double.parseDouble(valueText);
                        row.put(alias, value);
                    } catch (Exception e) {
                        continue;
                    }
                }

                // Store the reports associated with the current year.
                if (!yeartop10.isEmpty()) {
                    this.top10byyear.put(year, yeartop10);
                }

                if (!yearheadlines.isEmpty()) {
                    this.headlinesbyyear.put(year, yearheadlines);
                }
            }

        } finally {
            if (br != null) {
                br.close();
            }
        }

        return this.pricesbyyear.size();
    }


    /**
     * Resolves the delimiter provided by the client.
     * Null, empty and escaped tab values default to a tab character.
     */
    private String Delimiter(String d) {

        if (d == null) {
            return "\t";
        }

        d = d.trim();

        if (d.length() == 0) {
            return "\t";
        }

        if (d.equals("\\t")) {
            return "\t";
        }

        return d;
    }


    /**
     * Normalizes a product name or alias for lookup purposes.
     * Removes selected special characters and converts it to lowercase.
     */
    private String replace(String s) {

        if (s == null) {
            return "";
        }

        s = s.toLowerCase(Locale.ROOT);
        s = s.replace("(", "");
        s = s.replace(")", "");
        s = s.replace("-", "");
        s = s.replace("/", "");
        s = s.replace(" ", "");

        return s;
    }


    /**
     * Resolves a product name to its corresponding alias.
     * Unknown products retain their original names.
     */
    private String findAlias(String t) {

        // Use a complete normalized match to avoid incorrect substring matches.
        String alias = this.keyToAlias.get(replace(t));

        return alias != null ? alias : t;
    }


    /**
     * Splits a data line using the specified delimiter.
     * Supports quoted CSV fields and escaped quotation marks
     * within a single line.
     */
    private String[] splitDataLine(String line, String delimiter) throws IOException {

        // Treat non-CSV delimiters as literal strings rather than regex patterns.
        if (!",".equals(delimiter)) {
            return line.split(Pattern.quote(delimiter), -1);
        }

        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {

            char ch = line.charAt(i);

            if (ch == '"') {

                // Two consecutive quotes inside a quoted field represent one quote.
                if (quoted && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    field.append('"');
                    i++;

                } else {
                    quoted = !quoted;
                }

            } else if (ch == ',' && !quoted) {

                // A comma outside a quoted field marks the end of a field.
                fields.add(field.toString());
                field.setLength(0);

            } else {

                field.append(ch);
            }
        }

        // Reject malformed CSV fields with unclosed quotation marks.
        if (quoted) {
            throw new IOException("Unclosed CSV field");
        }

        fields.add(field.toString());

        return fields.toArray(new String[fields.size()]);
    }


    /**
     * UC1: Initializes the controller using the data and metadata
     * paths specified in the INI configuration file.
     */
    @Override
    public int initializeFromIni(String iniPath, String delimiter) throws IOException {

        delimiter = Delimiter(delimiter);

        // Validate the configuration path and delimiter.
        if (iniPath == null || iniPath.trim().isEmpty()) {
            throw new IllegalArgumentException("iniPath error");
        }

        if (delimiter == null || delimiter.length() == 0) {
            throw new IllegalArgumentException("delimiter error");
        }

        HashMap<String,String> config = readConfigIniPaths(iniPath);

        String dataPath = config.get("dataFile");
        String metadataPath = config.get("metadataFile");

        // Both file paths are required for initialization.
        if (dataPath == null || dataPath.trim().isEmpty()) {
            throw new IllegalArgumentException("dataFile missing");
        }

        if (metadataPath == null || metadataPath.trim().isEmpty()) {
            throw new IllegalArgumentException("metadataFile missing");
        }

        // Metadata must be loaded before the measurements.
        load_metadatatsv(metadataPath);

        return load_datatsv(dataPath, delimiter);
    }


    /**
     * Loads a data file directly without an INI configuration.
     * Product column headers are used as aliases when metadata
     * is not available.
     */
    @Override
    public void loadFile(String path, String delimiter) throws IOException {

        delimiter = Delimiter(delimiter);

        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("path error");
        }

        if (delimiter == null || delimiter.length() == 0) {
            throw new IllegalArgumentException("delimiter error");
        }

        // Reset all previously loaded data and metadata.
        this.aliasToTitle.clear();
        this.aliasToCategory.clear();
        this.pricesbyyear.clear();
        this.titleToAlias.clear();
        this.top10byyear.clear();
        this.headlinesbyyear.clear();
        this.aliasorder.clear();
        this.keyToAlias.clear();

        BufferedReader br = null;

        try {
            br = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8);

            String header = br.readLine();

            if (header == null) {
                throw new IllegalArgumentException("file has no header");
            }

            String[] columns = splitDataLine(header, delimiter);

            // Generate basic metadata from the product column headers.
            for (int i = 1; i < columns.length; i++) {

                String title = columns[i].trim();

                if (title.length() == 0) {
                    continue;
                }

                if (title.equals("CommodityTop10") || title.equals("News Headline")) {
                    continue;
                }

                String alias = title;

                this.aliasToTitle.put(alias, alias);
                this.aliasToCategory.put(alias, "Unknown");
                this.titleToAlias.put(title.toLowerCase(Locale.ROOT), alias);
                this.keyToAlias.put(replace(title), alias);
                this.keyToAlias.put(replace(alias), alias);

                if (!this.aliasorder.contains(alias)) {
                    this.aliasorder.add(alias);
                }
            }

        } finally {
            if (br != null) {
                br.close();
            }
        }

        load_datatsv(path, delimiter);
    }


    /**
     * Returns all available years in ascending order.
     * Uses the Selection Sort algorithm.
     */
    private int[] getSortedYears() {

        int n = pricesbyyear.size();
        int[] years = new int[n];
        int i = 0;

        // Copy the year keys from the HashMap into an array.
        for (Integer y : this.pricesbyyear.keySet()) {
            years[i] = y;
            i = i + 1;
        }

        // Find the smallest remaining year and move it into position.
        for (int a = 0; a < n - 1; a = a + 1) {

            int min = a;

            for (int b = a + 1; b < n; b++) {

                if (years[b] < years[min]) {
                    min = b;
                }
            }

            if (min != a) {
                int tmp = years[a];
                years[a] = years[min];
                years[min] = tmp;
            }
        }

        return years;
    }


    /**
     * Returns all available years and their associated measurements,
     * Top-10 products and headlines.
     */
    @Override
    public List<YearDTO> listYears() {

        List<YearDTO> out = new ArrayList<>();
        int[] years = getSortedYears();

        for (int y : years) {

            HashMap<String,Double> row = this.pricesbyyear.get(y);
            List<MeasurementDTO> ms = new ArrayList<>();

            // Preserve the original product column order.
            if (row != null) {

                for (String alias : this.aliasorder) {

                    Double v = row.get(alias);

                    if (v != null) {
                        ms.add(new MeasurementDTO(y, alias, v.doubleValue()));
                    }
                }
            }

            ArrayList<String> t10 = this.top10byyear.get(y);
            ArrayList<String> hl = this.headlinesbyyear.get(y);

            if (t10 == null) {
                t10 = new ArrayList<String>();
            }

            if (hl == null) {
                hl = new ArrayList<String>();
            }

            // Return copies to protect the controller's internal lists.
            out.add(new YearDTO(y, ms, new ArrayList<>(t10), new ArrayList<>(hl)));
        }

        return out;
    }


    /**
     * Returns all available products and their historical measurements.
     */
    @Override
    public List<ProductDTO> listProducts() {

        List<ProductDTO> out = new ArrayList<>();
        int[] years = getSortedYears();

        for (String alias : this.aliasorder) {

            List<MeasurementDTO> ms = new ArrayList<>();

            // Collect the available measurements for each product.
            for (int y : years) {

                HashMap<String,Double> row = pricesbyyear.get(y);

                if (row == null) {
                    continue;
                }

                Double v = row.get(alias);

                if (v != null) {
                    ms.add(new MeasurementDTO(y, alias, v.doubleValue()));
                }
            }

            out.add(new ProductDTO(alias, ms));
        }

        return out;
    }


    /**
     * UC3: Returns all product measurements for a specific year.
     * Returns null if the requested year is not available.
     */
    @Override
    public YearDTO getYearMeasurements(int year) {

        HashMap<String,Double> row = pricesbyyear.get(year);

        if (row == null) {
            return null;
        }

        List<MeasurementDTO> ms = new ArrayList<>();

        for (String alias : this.aliasorder) {

            Double v = row.get(alias);

            if (v != null) {
                ms.add(new MeasurementDTO(year, alias, v.doubleValue()));
            }
        }

        ArrayList<String> t10 = this.top10byyear.get(year);
        ArrayList<String> hl = this.headlinesbyyear.get(year);

        if (t10 == null) {
            t10 = new ArrayList<String>();
        }

        if (hl == null) {
            hl = new ArrayList<String>();
        }

        // Protect internal report lists from external modifications.
        return new YearDTO(year, ms, new ArrayList<>(t10), new ArrayList<>(hl));
    }


    /**
     * UC4: Returns all historical measurements for a product alias.
     */
    @Override
    public ProductDTO getProductMeasurements(String productName) {

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is empty");
        }

        String alias = productName.trim();
        List<MeasurementDTO> ms = new ArrayList<>();
        int[] years = getSortedYears();

        // Retrieve the product's available measurements in chronological order.
        for (int y : years) {

            HashMap<String,Double> row = pricesbyyear.get(y);

            if (row == null) {
                continue;
            }

            Double v = row.get(alias);

            if (v != null) {
                ms.add(new MeasurementDTO(y, alias, v.doubleValue()));
            }
        }

        return new ProductDTO(alias, ms);
    }


    /**
     * UC4: Filters product measurements within an inclusive year range.
     */
    @Override
    public ProductDTO filterProductMeasurements(String productName, int minYear, int maxYear) {

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is empty");
        }

        // Reject an invalid year range.
        if (minYear > maxYear) {
            throw new IllegalArgumentException("minYear>maxYear");
        }

        String alias = productName.trim();
        List<MeasurementDTO> ms = new ArrayList<>();
        int[] years = getSortedYears();

        for (int y : years) {

            // Ignore years outside the requested range.
            if (y < minYear || y > maxYear) {
                continue;
            }

            HashMap<String,Double> row = pricesbyyear.get(y);

            if (row == null) {
                continue;
            }

            Double v = row.get(alias);

            if (v != null) {
                ms.add(new MeasurementDTO(y, alias, v.doubleValue()));
            }
        }

        return new ProductDTO(alias, ms);
    }


    /**
     * UC5: Reports the years in which a product appeared in the Top-10,
     * together with its corresponding headlines.
     */
    @Override
    public List<ProductHighlightDTO> reportProductHighlights(String productAlias) {

        if (productAlias == null || productAlias.trim().isEmpty()) {
            throw new IllegalArgumentException("Product alias is empty");
        }

        String alias = productAlias.trim();

        // Resolve the input using the available product metadata.
        String mapped = this.titleToAlias.get(alias.toLowerCase(Locale.ROOT));

        if (mapped == null) {
            mapped = this.keyToAlias.get(replace(alias));
        }

        if (mapped != null) {
            alias = mapped;
        }

        String aliastolowerc = alias.toLowerCase(Locale.ROOT);

        List<ProductHighlightDTO> out = new ArrayList<>();
        int[] years = getSortedYears();

        for (int y : years) {

            ArrayList<String> t10 = this.top10byyear.get(y);

            if (t10 == null) {
                continue;
            }

            int pos = -1;

            // Find the first matching product in the yearly Top-10 list.
            for (int i = 0; i < t10.size(); i++) {

                String a = t10.get(i);

                if (a != null && a.trim().toLowerCase(Locale.ROOT).equals(aliastolowerc)) {
                    pos = i;
                    break;
                }
            }

            // The product did not appear in the Top-10 for this year.
            if (pos == -1) {
                continue;
            }

            ArrayList<String> hl = this.headlinesbyyear.get(y);
            String headline = "";

            // Match the headline using the product's position.
            if (hl != null && pos < hl.size()) {
                headline = hl.get(pos);
            }

            out.add(new ProductHighlightDTO(y, headline));
        }

        return out;
    }


    /**
     * UC6: Reports Top-10 appearances of products belonging
     * to the requested category.
     */
    @Override
    public List<CategoryHighlightDTO> reportCategoryHighlights(String category) {

        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category is empty");
        }

        String cat = category.trim();
        List<CategoryHighlightDTO> out = new ArrayList<>();
        int[] years = getSortedYears();

        for (int y : years) {

            ArrayList<String> t10 = this.top10byyear.get(y);

            if (t10 == null) {
                continue;
            }

            ArrayList<String> hl = this.headlinesbyyear.get(y);

            for (int i = 0; i < t10.size(); i++) {

                String alias = t10.get(i);
                String c = this.aliasToCategory.get(alias);

                // Include only products belonging to the requested category.
                if (c != null && c.equals(cat)) {

                    String headline = "";

                    if (hl != null && i < hl.size()) {
                        headline = hl.get(i);
                    }

                    out.add(new CategoryHighlightDTO(y, alias, headline));
                }
            }
        }

        return out;
    }


    /**
     * UC7: Computes the minimum, average, maximum and last available
     * price for each product.
     */
    @Override
    public List<ProductStatsDTO> computeProductStats() {

        List<ProductStatsDTO> out = new ArrayList<>();
        int[] years = getSortedYears();

        for (String alias : this.aliasorder) {

            boolean has = false;
            double min = 0.0;
            double max = 0.0;
            double sum = 0.0;
            int count = 0;
            double lastValue = 0.0;

            for (int y : years) {

                HashMap<String,Double> row = pricesbyyear.get(y);

                if (row == null) {
                    continue;
                }

                Double v = row.get(alias);

                // Missing measurements are excluded from the calculations.
                if (v == null) {
                    continue;
                }

                double val = v.doubleValue();

                // Initialize the minimum and maximum using the first valid value.
                if (has == false) {
                    min = val;
                    max = val;
                    has = true;
                }

                if (val < min) {
                    min = val;
                }

                if (val > max) {
                    max = val;
                }

                sum = sum + val;
                count = count + 1;

                // Years are sorted, so this becomes the latest available value.
                lastValue = val;
            }

            // Calculate statistics only for products with valid measurements.
            if (count > 0) {

                double avg = sum / count;

                out.add(new ProductStatsDTO(alias, min, avg, max, lastValue));
            }
        }

        return out;
    }


    /**
     * UC8: Counts the number of Top-10 appearances for each product
     * and returns the results in descending order.
     */
    @Override
    public List<Top10AppearanceDTO> computeTop10ProductAppearances() {

        HashMap<String,Integer> count = new HashMap<>();

        // Count product appearances across all available years.
        for (Integer y : this.top10byyear.keySet()) {

            ArrayList<String> list = this.top10byyear.get(y);

            if (list == null) {
                continue;
            }

            for (String alias : list) {

                if (alias == null) {
                    continue;
                }

                Integer cur = count.get(alias);

                if (cur == null) {
                    cur = 0;
                }

                count.put(alias, cur + 1);
            }
        }

        List<Top10AppearanceDTO> out = new ArrayList<>();

        // Convert the appearance counts into DTO objects.
        for (String alias : count.keySet()) {

            Integer c = count.get(alias);
            int value = 0;

            if (c != null) {
                value = c.intValue();
            }

            out.add(new Top10AppearanceDTO(alias, value));
        }

        // Sort products by appearance count in descending order.
        int n = out.size();

        for (int i = 0; i < n - 1; i++) {

            for (int j = i + 1; j < n; j++) {

                Top10AppearanceDTO a = out.get(i);
                Top10AppearanceDTO b = out.get(j);

                if (b.getCount() > a.getCount()) {
                    out.set(i, b);
                    out.set(j, a);
                }
            }
        }

        return out;
    }


    /**
     * UC9: Counts the Top-10 appearances of products grouped
     * by their corresponding categories.
     */
    @Override
    public List<Top10AppearanceDTO> computeTop10CategoryAppearances() {

        HashMap<String,Integer> count = new HashMap<>();

        for (Integer y : this.top10byyear.keySet()) {

            ArrayList<String> list = this.top10byyear.get(y);

            if (list == null) {
                continue;
            }

            for (String alias : list) {

                String cat = this.aliasToCategory.get(alias);

                // Products without category metadata are excluded.
                if (cat == null) {
                    continue;
                }

                Integer cur = count.get(cat);

                if (cur == null) {
                    cur = 0;
                }

                // Each product appearance contributes to its category's count.
                count.put(cat, cur + 1);
            }
        }

        List<Top10AppearanceDTO> out = new ArrayList<>();

        // Convert the category counts into DTO objects.
        for (String cat : count.keySet()) {

            Integer c = count.get(cat);
            int value = 0;

            if (c != null) {
                value = c.intValue();
            }

            out.add(new Top10AppearanceDTO(cat, value));
        }

        // Sort categories by appearance count in descending order.
        int n = out.size();

        for (int i = 0; i < n - 1; i++) {

            for (int j = i + 1; j < n; j++) {

                Top10AppearanceDTO a = out.get(i);
                Top10AppearanceDTO b = out.get(j);

                if (b.getCount() > a.getCount()) {
                    out.set(i, b);
                    out.set(j, a);
                }
            }
        }

        return out;
    }


    /**
     * UC2: Returns the complete report of product prices
     * across all available years.
     */
    @Override
    public List<YearDTO> reportAllYearsAllProductPrices() {

        return listYears();
    }
}
