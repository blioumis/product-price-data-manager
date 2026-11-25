package testControllermethods;

import controller.ControllerFactory;
import controller.IController;

import dto.Top10AppearanceDTO;
import dto.YearDTO;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

/**
 * Regression tests for the post-submission controller improvements.
 *
 * Verifies that the original dataset results remain unchanged
 * and that the improved parsing and data protection mechanisms
 * behave as expected.
 */
public class testForControllerRegression {

    // Temporary test files are removed automatically by JUnit.
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    /**
     * Creates a temporary UTF-8 data file for testing.
     */
    private Path writeFile(String name, String... lines) throws Exception {

        Path file = folder.newFile(name).toPath();

        Files.write(file, Arrays.asList(lines), StandardCharsets.UTF_8);

        return file;
    }


    /**
     * Loads the original academic dataset using the controller factory.
     */
    private IController loadOriginalData() throws Exception {

        IController c = ControllerFactory.createController();

        // The original dataset contains 64 distinct years.
        assertEquals(64, c.initializeFromIni(
                "src/main/resources/config/config.ini", "\\t"));

        return c;
    }


    /**
     * Calculates the total number of appearances from a report.
     */
    private int totalAppearances(List<Top10AppearanceDTO> appearances) {

        int total = 0;

        for (Top10AppearanceDTO item : appearances) {
            total += item.getCount();
        }

        return total;
    }


    /**
     * Verifies that the original dataset produces the same results
     * after the controller improvements.
     */
    @Test
    public void testOriginalDatasetCounts() throws Exception {

        IController c = loadOriginalData();

        // Verify the number of products with price columns.
        assertEquals(31, c.listProducts().size());

        // Verify the total number of product appearances.
        assertEquals(610,
                totalAppearances(c.computeTop10ProductAppearances()));

        // Products without category metadata are excluded from this report.
        assertEquals(546,
                totalAppearances(c.computeTop10CategoryAppearances()));

        // Verify the original product and category highlight counts.
        assertEquals(64,
                c.reportProductHighlights("Gold").size());

        assertEquals(128,
                c.reportCategoryHighlights("PreciousMetals").size());
    }


    /**
     * Verifies that special regex characters can be used
     * as literal file delimiters.
     */
    @Test
    public void testLiteralDelimiter() throws Exception {

        // The pipe character must be treated as a literal delimiter.
        Path file = writeFile("prices.txt",
                "year|Gold|Silver",
                "2020|12.5|3.5");

        IController c = ControllerFactory.createController();

        c.loadFile(file.toString(), "|");

        // Verify that both product measurements were loaded.
        assertEquals(2,
                c.getYearMeasurements(2020).getMeasurements().size());

        // Verify that the numerical value was parsed correctly.
        assertEquals(12.5,
                c.getProductMeasurements("Gold")
                        .getMeasurements().get(0).getValue(),
                0.001);
    }


    /**
     * Verifies support for quoted CSV fields, embedded commas
     * and UTF-8 characters.
     */
    @Test
    public void testCsvQuotedFieldsAndUtf8() throws Exception {

        Path file = writeFile("prices.csv",
                "year,Café,Gold,CommodityTop10,News Headline",
                "2020,2.5,10.0,\"Café, Gold\",\"Café, market news | Gold rises\"");

        IController c = ControllerFactory.createController();

        c.loadFile(file.toString(), ",");

        // Verify that UTF-8 product names are preserved.
        assertEquals(2.5,
                c.getProductMeasurements("Café")
                        .getMeasurements().get(0).getValue(),
                0.001);

        // Verify that commas inside quoted fields are preserved.
        assertEquals("Café, market news",
                c.reportProductHighlights("Café").get(0).getHeadline());

        // Verify the association between products and their headlines.
        assertEquals("Gold rises",
                c.reportProductHighlights("Gold").get(0).getHeadline());
    }


    /**
     * Verifies that unknown product names are not incorrectly
     * associated with existing aliases through substring matching.
     */
    @Test
    public void testUnknownAliasDoesNotMatchSubstring() throws Exception {

        Path file = writeFile("aliases.tsv",
                "year\tGold\tCommodityTop10\tNews Headline",
                "2020\t12.5\t\"GoldCoin\"\tGoldCoin news");

        IController c = ControllerFactory.createController();

        c.loadFile(file.toString(), "\\t");

        // GoldCoin must remain a separate product from Gold.
        assertEquals("GoldCoin",
                c.computeTop10ProductAppearances().get(0).getName());

        assertTrue(c.reportProductHighlights("Gold").isEmpty());

        assertEquals("GoldCoin news",
                c.reportProductHighlights("GoldCoin").get(0).getHeadline());
    }


    /**
     * Verifies that modifying a returned YearDTO does not modify
     * the controller's internal Top-10 and headline lists.
     */
    @Test
    public void testYearDtoDoesNotExposeControllerLists() throws Exception {

        IController c = loadOriginalData();

        YearDTO year = c.getYearMeasurements(1960);

        int originalSize = year.getTop10Aliases().size();
        int originalHeadlines = year.getTop10Headlines().size();

        // Attempt to modify the lists returned by the DTO.
        year.getTop10Aliases().clear();
        year.getTop10Headlines().clear();

        // The original controller data must remain unchanged.
        assertEquals(originalSize,
                c.getYearMeasurements(1960).getTop10Aliases().size());

        assertEquals(originalHeadlines,
                c.getYearMeasurements(1960).getTop10Headlines().size());

        // Verify that listYears also returns independent report lists.
        c.listYears().get(0).getTop10Aliases().clear();

        assertEquals(originalSize,
                c.getYearMeasurements(1960).getTop10Aliases().size());
    }
}
