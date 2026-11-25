package testControllermethods;
import dto.YearDTO;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


public class testForUC3 {
	private controller.ControllerImplementation c;

	@Before
	public void setUp() throws Exception {
		c = new controller.ControllerImplementation();
		String iniPath = "src/main/resources/config/config.ini";
		c.initializeFromIni(iniPath, "\t");
	}

	@Test
	public void testForUC1_HappyDay() throws Exception{
		List<YearDTO> allYears = c.listYears();
		assertNotNull(allYears);
		assertFalse(allYears.isEmpty()); // na yparxei ena etos toulaxiston
		int protoEtos= allYears.get(0).getYear(); 
		YearDTO epilegmenoEtos = c.getYearMeasurements(protoEtos); // apo to proto etos poy yparxei sti lista kanoyme proboli timon
		
		assertNotNull(epilegmenoEtos);
		assertEquals(protoEtos, epilegmenoEtos.getYear());
		assertNotNull(epilegmenoEtos.getMeasurements()); // na min einai null oi metriseis
		assertFalse(epilegmenoEtos.getMeasurements().isEmpty());  // na min einai kenes oi metriseis
	}
	
	@Test
	public void testForUC1_RainyDay(){
		List<YearDTO> allYears = c.listYears();
		assertNotNull(allYears);
		assertFalse(allYears.isEmpty()); // na yparxei ena etos toulaxiston
		int etos= allYears.get(0).getYear(); 
		for(YearDTO y : allYears) {
			if(y.getYear()> etos) {
				etos = y.getYear();
			}
		}
		int miEgkuroEtos= etos +1;
		YearDTO akuroEtos = c.getYearMeasurements(miEgkuroEtos);
		
		assertNull(akuroEtos);
	}
}
