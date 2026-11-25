package testControllermethods;

import java.util.List;
import dto.ProductDTO;
import dto.MeasurementDTO;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests UC4: Product measurements and filtering by year range.
 */
public class testForUC4 {

    private controller.ControllerImplementation c;

    @Before
    public void setUp() throws Exception {

        c = new controller.ControllerImplementation();

        String iniPath = "src/main/resources/config/config.ini";

        // Load the dataset before each test.
        c.initializeFromIni(iniPath, "\t");
    }


    /**
     * Verifies that product measurements can be retrieved and filtered
     * using a valid year range.
     */
    @Test
    public void testForUC4_HappyDay() throws Exception {

        List<ProductDTO> allProducts = c.listProducts();

        assertNotNull(allProducts);
        assertFalse(allProducts.isEmpty());

        // Select the first available product.
        ProductDTO protoProduct = allProducts.get(0);
        String productName = protoProduct.getName();

        ProductDTO allYearsForProduct = c.getProductMeasurements(productName);

        // Verify that the selected product contains valid measurements.
        assertNotNull(allYearsForProduct);
        assertEquals(productName, allYearsForProduct.getName());
        assertNotNull(allYearsForProduct.getMeasurements());
        assertFalse(allYearsForProduct.getMeasurements().isEmpty());

        int minYear = Integer.MAX_VALUE;
        int maxYear = Integer.MIN_VALUE;

        // Determine the earliest and latest available measurement years.
        for (MeasurementDTO m : allYearsForProduct.getMeasurements()) {

            int y = m.getYear();

            if (y < minYear) {
                minYear = y;
            }

            if (y > maxYear) {
                maxYear = y;
            }
        }

        // Request measurements within the valid year range.
        ProductDTO filtered = c.filterProductMeasurements(
                productName, minYear, maxYear);

        // Verify that the filtered product contains measurements.
        assertNotNull(filtered);
        assertEquals(productName, filtered.getName());
        assertNotNull(filtered.getMeasurements());
        assertFalse(filtered.getMeasurements().isEmpty());
    }


    /**
     * Verifies that a reversed year range is rejected.
     */
    @Test
    public void testForUC4_RainyDay() throws Exception {

        List<ProductDTO> allProducts = c.listProducts();

        assertNotNull(allProducts);
        assertFalse(allProducts.isEmpty());

        ProductDTO protoProduct = allProducts.get(0);
        String productName = protoProduct.getName();

        ProductDTO allYearsForProduct = c.getProductMeasurements(productName);

        assertNotNull(allYearsForProduct);
        assertEquals(productName, allYearsForProduct.getName());
        assertNotNull(allYearsForProduct.getMeasurements());
        assertFalse(allYearsForProduct.getMeasurements().isEmpty());

        int minYear = Integer.MAX_VALUE;
        int maxYear = Integer.MIN_VALUE;

        // Determine the available year range.
        for (MeasurementDTO m : allYearsForProduct.getMeasurements()) {

            int y = m.getYear();

            if (y < minYear) {
                minYear = y;
            }

            if (y > maxYear) {
                maxYear = y;
            }
        }

        // Reverse the boundaries to create an invalid range.
        int wrongMin = maxYear;
        int wrongMax = minYear;

        try {

            c.filterProductMeasurements(productName, wrongMin, wrongMax);

            // The test must fail if the invalid range is accepted.
            fail("Expected IllegalArgumentException for a reversed year range");

        } catch (IllegalArgumentException e) {

            // Expected exception.
        }
    }
}
