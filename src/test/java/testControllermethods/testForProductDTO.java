package testControllermethods;

import static org.junit.Assert.*;
import dto.MeasurementDTO;
import dto.ProductDTO;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class testForProductDTO {
	private ProductDTO product;
	private List<MeasurementDTO> m;

	@Before
	public void setUp() throws Exception {
		MeasurementDTO m2= new MeasurementDTO(2020,"oil",1.23);
		MeasurementDTO m3= new MeasurementDTO(2021,"oil",1.50);
		
		m = Arrays.asList(m2,m3);
		
		product = new ProductDTO("oil",m); //Δημιουργουμε το productDTO με ονομα προιοντος και τη λιστα μετρησεων m 
	}

	@Test
	public void testMeasurementsSize() {
		assertEquals(2,product.getMeasurements().size());
	}
	
	@Test
	public void testMeasurementsGetName() {
		assertEquals("oil",product.getName()); 
	}

}
