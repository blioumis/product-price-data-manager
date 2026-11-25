package testControllermethods;

import static org.junit.Assert.*;
import dto.MeasurementDTO;

import org.junit.Before;
import org.junit.Test;

public class testForMeasurementsDTO {
	private MeasurementDTO m;

	@Before
	public void setUp() throws Exception {
		m=new MeasurementDTO(2020, "oli", 1.23);
	}

	@Test
	public void testGetYear() {
		assertEquals(2020,m.getYear());
	}
	
	@Test
	public void testGetProductName() {
		assertEquals("oli", m.getProductName());
	}
	
	

}
