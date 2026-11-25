package testControllermethods;

import java.util.List;
import dto.ProductHighlightDTO;
import dto.ProductDTO;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests UC5: Product highlights and associated headlines.
 */
public class testForUC5 {

    private controller.ControllerImplementation c;

    @Before
    public void setUp() throws Exception {

        c = new controller.ControllerImplementation();

        String iniPath = "src/main/resources/config/config.ini";

        // Load the dataset before each test.
        c.initializeFromIni(iniPath, "\t");
    }


    /**
     * Verifies that highlights can be retrieved for a product
     * that appears in the Top-10.
     */
    @Test
    public void testForUC5_HappyDay() throws Exception {

        List<ProductDTO> allProducts = c.listProducts();

        assertNotNull(allProducts);
        assertFalse(allProducts.isEmpty());

        ProductDTO chosenProduct = null;
        List<ProductHighlightDTO> highlights = null;

        // Find a product with at least one Top-10 appearance.
        for (ProductDTO p : allProducts) {

            List<ProductHighlightDTO> h = c.reportProductHighlights(p.getName());

            if (h != null && !h.isEmpty()) {
                chosenProduct = p;
                highlights = h;
                break;
            }
        }

        // Verify that a suitable product and its highlights were found.
        assertNotNull(chosenProduct);
        assertNotNull(highlights);
        assertFalse(highlights.isEmpty());

        // Validate the year and headline fields of each result.
        for (ProductHighlightDTO A : highlights) {

            assertTrue(A.getYear() > 0);
            assertNotNull(A.getHeadline());
        }
    }


    /**
     * Verifies that an unknown product returns an empty highlights list.
     */
    @Test
    public void testForUC5_RainyDay() throws Exception {

        String anuparktoProion = "anuparktoProion";

        List<ProductHighlightDTO> highlights =
                c.reportProductHighlights(anuparktoProion);

        // Unknown products must not produce highlight records.
        assertNotNull(highlights);
        assertTrue(highlights.isEmpty());
    }
}
