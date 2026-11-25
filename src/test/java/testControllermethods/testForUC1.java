package testControllermethods;

import dto.YearDTO;
import java.util.List;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests UC1: Initialization from an INI configuration file.
 */
public class testForUC1 {

    private controller.ControllerImplementation c;

    @Before
    public void setUp() throws Exception {

        c = new controller.ControllerImplementation();

        String iniPath = "src/main/resources/config/config.ini";

        if (iniPath == null || iniPath.trim().isEmpty()) {
            throw new IllegalArgumentException("iniPath error");
        }

        // Initialize the controller using the original configuration file.
        c.initializeFromIni(iniPath, "\t");
    }


    /**
     * Verifies that a valid configuration file loads the dataset successfully.
     */
    @Test
    public void testForUC1_HappyDay() throws Exception {

        String inipath = "src/main/resources/config/config.ini";

        int A = c.initializeFromIni(inipath, "\t");

        // At least one year must be loaded successfully.
        assertTrue(A > 0);

        List<YearDTO> allYears = c.reportAllYearsAllProductPrices();

        // Verify that the loaded data is available through the controller.
        assertNotNull(allYears);
        assertFalse(allYears.isEmpty());
    }


    /**
     * Verifies that an empty configuration path is rejected.
     */
    @Test
    public void testForUC1_RainyDay() throws Exception {

        try {

            String kenoIniPath = "";

            c.initializeFromIni(kenoIniPath, "\t");

            // The test must fail if the expected exception is not thrown.
            fail("Expected IllegalArgumentException for an empty configuration path");

        } catch (IllegalArgumentException e) {

            // Expected exception.
        }
    }
}
