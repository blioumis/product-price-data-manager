package testControllermethods;
import java.util.List;
import dto.Top10AppearanceDTO;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class testForUC8 {
	private controller.ControllerImplementation c;

	@Before
	public void setUp() throws Exception {
		c = new controller.ControllerImplementation();
		String inipath ="src/main/resources/config/config.ini" ;
		c.initializeFromIni(inipath, "\t");
	}

	@Test
	public void testForUC8_HappyDay() throws Exception{
		List<Top10AppearanceDTO> top10Proionta = c.computeTop10ProductAppearances(); // gemizei ti lista me ola ta proionta pou exoyn mpei sto top10
		assertNotNull(top10Proionta);  // na min einai null h lista
		assertFalse(top10Proionta.isEmpty()); // na min einai kenh i lista
		
		for(Top10AppearanceDTO productOfTop10: top10Proionta) { // gia kathe proion ths listas na min einai null h keno string to onoma kai to count>0
			assertNotNull(productOfTop10.getName());
			assertFalse(productOfTop10.getName().trim().isEmpty());
			assertTrue(productOfTop10.getCount()>0);
		}
	}
	
	@Test
	public void testForUC8_RainyDay() throws Exception{
		controller.ControllerImplementation kenadedomena = new controller.ControllerImplementation();
		List<Top10AppearanceDTO> kenoTop10 = kenadedomena.computeTop10ProductAppearances(); // kaloyme xoris na exoyme fortosei dedomena
		assertNotNull(kenoTop10); // oxi null 
		assertTrue(kenoTop10.isEmpty()); // theloyme na doume keni lista
	}
}
