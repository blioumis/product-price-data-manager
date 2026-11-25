package testControllermethods;
import dto.Top10AppearanceDTO;
import java.util.List;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;



public class testForUC9 {
	private controller.ControllerImplementation c;

	@Before
	public void setUp() throws Exception {
		c=new controller.ControllerImplementation();
		String inipath ="src/main/resources/config/config.ini" ;
		c.initializeFromIni(inipath, "\t");
	}

	@Test
	public void testForUC9_HappyDay() throws Exception{
		List<Top10AppearanceDTO> categTop10 = c.computeTop10CategoryAppearances(); // gemizei ti lista me oles tis kathgories poy exoyn mpei sta Top10
		assertNotNull(categTop10); // na min einai null
		assertFalse(categTop10.isEmpty()); // na min einai keni
		
		for(Top10AppearanceDTO categories : categTop10) { // gia kathe kathgoria na to onoma na min einai null h keno String kai to count tis >0
			assertNotNull(categories.getName());
			assertFalse(categories.getName().trim().isEmpty());
			assertTrue(categories.getCount()>0);
		}
	}
	
	@Test
	public void testForUc9_RainyDay() throws Exception{ // idia logikh me to Test gia to  UC8 alla gia kathgories
		controller.ControllerImplementation kenadedomena = new controller.ControllerImplementation();
		List<Top10AppearanceDTO> kenoTop10 = kenadedomena.computeTop10CategoryAppearances();
		assertNotNull(kenoTop10);
		assertTrue(kenoTop10.isEmpty());
		
	}

}
