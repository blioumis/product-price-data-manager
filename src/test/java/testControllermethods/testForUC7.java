package testControllermethods;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import dto.ProductStatsDTO;
public class testForUC7 {
	private controller.ControllerImplementation c;

	@Before
	public void setUp() throws Exception {
		c = new controller.ControllerImplementation();
		String inipath ="src/main/resources/config/config.ini" ;
		c.initializeFromIni(inipath, "\t");
	}

	@Test
	public void testForUC7_HappyDay() {
		List<ProductStatsDTO> statsList = c.computeProductStats(); // pairnoume mia lista me statistika
		assertNotNull(statsList); // blepoyme na mhn einai null h lista
		assertFalse(statsList.isEmpty()); // blepoyme na mhn einai kenh h lista
		
		for(ProductStatsDTO product: statsList) {  // blepoyme gia kathe proion tis lista ta parakato
			assertNotNull(product.getProduct()); // na min einai Null
			assertFalse(product.getProduct().trim().isEmpty());  // na min einai keno sting
			
			double min = product.getMin();
			double max = product.getMax();
			double avg = product.getAverage();
			double last = product.getLastValue();
			
			assertTrue(min<=max);  // gia na stekoun ta dedomena mas
			assertTrue(avg>=min && avg<=max);
			assertTrue(last>=min && last<=max);
		}
	}
	
	@Test
	public void testForUC7_RainyDay() throws Exception{
		controller.ControllerImplementation keno= new controller.ControllerImplementation(); // δεν παιρναμε τα δεδομενα αρα ειναι
			List<ProductStatsDTO> keniStatsList = keno.computeProductStats();
			assertNotNull(keniStatsList);
			assertTrue(keniStatsList.isEmpty());
	}

}
