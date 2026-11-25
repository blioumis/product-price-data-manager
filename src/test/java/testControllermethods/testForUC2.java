package testControllermethods;
import java.util.List;
import dto.YearDTO;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class testForUC2 {
	private controller.ControllerImplementation c;

	@Before
	public void setUp() throws Exception {
		c= new controller.ControllerImplementation();
		String iniPath="src/main/resources/config/config.ini";
		c.initializeFromIni(iniPath,"\t");
	}

	@Test
	public void testForUC2_HappyDay() throws Exception{
		List<YearDTO> allYears = c.reportAllYearsAllProductPrices();
		assertNotNull(allYears);
		assertFalse(allYears.isEmpty());
		YearDTO firstYear = allYears.get(0);
		assertNotNull(firstYear.getMeasurements());    // oi metriseis gia to proto etos na min einai null.
		assertFalse(firstYear.getMeasurements().isEmpty()); // oi metriseis gia to proto etos na einai miden.
	}
	
	@Test
	public void testForUC2_RainyDay(){
		controller.ControllerImplementation c = new controller.ControllerImplementation();
		List<YearDTO> allYears = c.reportAllYearsAllProductPrices();
		assertNotNull(allYears); // den theloyme null alla keni lista.
		assertTrue(allYears.isEmpty()); // True an einai keni
	}
	

}
