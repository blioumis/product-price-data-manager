package testControllermethods;

import static org.junit.Assert.*;
import dto.YearDTO;
import dto.MeasurementDTO;
import java.util.List;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class testForYearDTO {
	private YearDTO year;
	private List<MeasurementDTO> m;
	private List<String> top10Aliases;
	private List<String> top10Headlines;

	@Before
	public void setUp() throws Exception { // Φτιαχνουμε 2 μετρησεις για το ετος 2020 
		MeasurementDTO m2 = new MeasurementDTO(2020,"oli",1.23);
		MeasurementDTO m3 = new MeasurementDTO(2020,"Cocoa",0.80);
		m = Arrays.asList(m2,m3); 
		
		top10Aliases= Arrays.asList("oil","Cocoa"); // δημιουργουμε λιστα για τα προιοντα
		
		top10Headlines = Arrays.asList("oil price changes","Cocoa price changes"); // δημιουργουμε λιστα με headlines για τα αντιστοιχα προιοντα
		
		year= new YearDTO(2020, m , top10Aliases, top10Headlines); // Φτιαχνουμε το YearDTO με ετος,μετρησεις και τις λιστες
	}

	@Test
	public void testGetYear() { // ελεγχος αν αποθηκευτηκε σωστα το ετος
		assertEquals(2020,year.getYear());
	}
	
	@Test 
	public void testMeasurementsStored() { // ελεγχος για αν ο αριθμος μετρησεων ειναι ιδιος με τον της λιστας που δωσαμε
		assertEquals(2,year.getMeasurements().size());
	}
	
	@Test
	public void testTop10HeadlinesStored() { // ελεγχος οτι αποθηκευτηκε σωστα η λιστα με τις κεφαλιδες
		assertEquals(top10Headlines, year.getTop10Headlines());
	}
	
	@Test
	public void testTop10AliasesStored() { // ελεγχος οτι αποθηκευτηκε σωστα η λιστα με τα προιοντα
		assertEquals(top10Aliases, year.getTop10Aliases());
	}

}
