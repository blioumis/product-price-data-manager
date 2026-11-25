package testControllermethods;

import java.util.List;
import dto.CategoryHighlightDTO;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests UC6: Category highlights and associated product headlines.
 */
public class testForUC6 {

    private controller.ControllerImplementation c;

    @Before
    public void setUp() throws Exception {

        c = new controller.ControllerImplementation();

        String iniPath = "src/main/resources/config/config.ini";

        // Load the dataset before each test.
        c.initializeFromIni(iniPath, "\t");
    }


    /**
     * Verifies that highlights are returned for an existing category.
     */
    @Test
    public void testForUC6_HappyDay() throws Exception {

        String katigoriaEnergy = "Energy";

        List<CategoryHighlightDTO> highlights =
                c.reportCategoryHighlights(katigoriaEnergy);

        // The selected category must contain highlight records.
        assertNotNull(highlights);
        assertFalse(highlights.isEmpty());

        // Validate the year, product and headline of each result.
        for (CategoryHighlightDTO h : highlights) {

            assertTrue(h.getYear() > 0);

            assertNotNull(h.getProduct());
            assertFalse(h.getProduct().trim().isEmpty());

            assertNotNull(h.getHeadline());
            assertFalse(h.getHeadline().trim().isEmpty());
        }
    }


    /**
     * Verifies that an unknown category returns an empty highlights list.
     */
    @Test
    public void testForUC6_RainyDay() throws Exception {

        String akurhkatigoria = "akurhkatigoria";

        List<CategoryHighlightDTO> highlights =
                c.reportCategoryHighlights(akurhkatigoria);

        // Unknown categories must not produce highlight records.
        assertNotNull(highlights);
        assertTrue(highlights.isEmpty());
    }
}
