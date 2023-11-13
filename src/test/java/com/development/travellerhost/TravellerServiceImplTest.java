package com.development.travellerhost;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import com.development.traveller.customexception.DuplicateResourceException;
import com.development.traveller.customexception.TravellerAlreadyDeactivatedException;
import com.development.traveller.customexception.TravellerAlredyExistsException;
import com.development.traveller.customexception.TravellerNotFoundException;
import com.development.travellerhost.dao.TravellerDocumentRepository;
import com.development.travellerhost.dao.TravellerRepository;
import com.development.travellerhost.model.DocumentType;
import com.development.travellerhost.model.Traveller;
import com.development.travellerhost.model.TravellerDocument;
import com.development.travellerhost.service.TravellerServiceImpl;

class TravellerServiceImplTest {

	@Mock
	private TravellerRepository travellerRepository;

	@Mock
	private TravellerDocumentRepository travellerDocumentRepository;

	@InjectMocks
	private TravellerServiceImpl travellerService;
	private Traveller traveller;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		traveller = createSampleTraveller();
		List<TravellerDocument> documents = createSampleDocuments();
		traveller.setDocuments(documents);
	}

	@Test
	void testCreateTraveller() throws TravellerAlredyExistsException {
	

		when(travellerRepository.findByEmailAndMobileNumber(any(), any())).thenReturn(Optional.empty());
		when(travellerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		Traveller createdTraveller = travellerService.createTraveller(traveller);

		assertNotNull(createdTraveller);
		assertEquals(traveller.getFirstName(), createdTraveller.getFirstName());
		assertEquals(traveller.getLastName(), createdTraveller.getLastName());
		assertEquals(traveller.getDateOfBirth(), createdTraveller.getDateOfBirth());
		assertEquals(2, createdTraveller.getDocuments().size());
		assertTrue(createdTraveller.getDocuments().get(0).isActive());
	}

	@Test
	void testCreateTravellerWithExistingTraveller() {
	    Traveller existingTraveller = createSampleTraveller();
	    traveller.setId(1L);

	    when(travellerRepository.findByEmailAndMobileNumber(any(), any())).thenReturn(Optional.of(existingTraveller));
	    when(travellerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

	    assertThrows(TravellerAlredyExistsException.class, () -> {
	        travellerService.createTraveller(traveller);
	    });
	}

	@Test
	void testDeactivateTraveller_AlreadyDeactivated1() {
	   
	    Traveller deactivatedTraveller = createSampleTraveller();
	    deactivatedTraveller.setActive(false);

	    when(travellerRepository.findByEmailAndMobileNumberAndFirstNameAndLastNameAndDateOfBirth(
	            anyString(), anyString(), anyString(), anyString(), anyString()))
	            .thenReturn(deactivatedTraveller);

	    TravellerAlreadyDeactivatedException exception = assertThrows(TravellerAlreadyDeactivatedException.class, () ->
	            travellerService.deactivateTraveller("Krishna", "Priya", "1994-04-27",
	                    "krishnavaradha@example.com", "1234567890"));

	    // Check if the exception message contains the expected message
	    assertTrue(exception.getMessage().contains("Traveller is already deactivated"));
	}

	@Test
	public void deactivateTraveller_Success() throws TravellerNotFoundException, TravellerAlreadyDeactivatedException {
		
	    when(travellerRepository.findByEmailAndMobileNumberAndFirstNameAndLastNameAndDateOfBirth(
	    		"krishnavaradha@example.com", "1234567890","Krishna", "Priya", "1994-04-27" ))
	            .thenReturn(traveller);
	    when(travellerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

	    Traveller deactivatedTraveller = travellerService.deactivateTraveller(
	            "Krishna", "Priya", "1994-04-27", "krishnavaradha@example.com", "1234567890");

	    assertFalse(deactivatedTraveller.getActive());
	}
	@Test
	void createTraveller_DataIntegrityViolationException() {
	    
	   
	    when(travellerRepository.findByEmailAndMobileNumber(any(), any())).thenReturn(Optional.empty());
		when(travellerRepository.save(any())).thenThrow(DataIntegrityViolationException.class);
	    
	    TravellerAlredyExistsException exception = assertThrows(TravellerAlredyExistsException.class, () -> travellerService.createTraveller(traveller));
	    assertEquals("Mobile number/Email Id already exists. Please use a different mobile number or Email Id.", exception.getMessage());
	}
	@Test
    void searchActiveTravellers_TravellerNotFoundException() {
       
        when(travellerRepository.searchActiveTravellers(anyString(), anyString(), any(DocumentType.class), anyString(), anyString()))
                .thenReturn(null);

        
        assertThrows(TravellerNotFoundException.class, () ->
                travellerService.searchActiveTravellers("krishnavaradha@example.com", "1234567890", DocumentType.PASSPORT, "AB123", "Country"));
    }
	@Test
    void searchActiveTravellers_SuccessfulSearch() throws TravellerNotFoundException {
      
        when(travellerRepository.searchActiveTravellers(anyString(), anyString(), any(DocumentType.class), anyString(), anyString()))
                .thenReturn(traveller);

        // Act
        Traveller foundTraveller = travellerService.searchActiveTravellers("krishnavaradha@example.com", "1234567890", DocumentType.ID_CARD, "ABC123", "Austria");

        // Assert
        assertNotNull(foundTraveller);
        assertEquals(traveller, foundTraveller);
    }


	private Traveller createSampleTraveller() {
		Traveller traveller = new Traveller();
		traveller.setFirstName("Krishna");
		traveller.setLastName("Priya");
		traveller.setDateOfBirth("1994-04-27");
		traveller.setEmail("krishnavaradha@example.com");
		traveller.setMobileNumber("1234567890");
		return traveller;
	}
	@Test
    void testDeactivateTraveller_TravellerNotFound() {
       
        when(travellerRepository.findByEmailAndMobileNumberAndFirstNameAndLastNameAndDateOfBirth(
                anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);

        
        assertThrows(TravellerNotFoundException.class, () ->
                travellerService.deactivateTraveller("Krishna", "Priya", "1994-04-27",
                        "krishnavaradha@example.com", "1234567890"));
    }
	
	@Test
    void testDeactivateTraveller_ExceptionThrown() {
       
        when(travellerRepository.findByEmailAndMobileNumberAndFirstNameAndLastNameAndDateOfBirth(
                anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(DataIntegrityViolationException.class);

        
        assertThrows(RuntimeException.class, () ->
                travellerService.deactivateTraveller("Krishna", "Priya", "1994-04-27",
                        "krishnavaradha@example.com", "1234567890"));
    }

	private List<TravellerDocument> createSampleDocuments() {
		TravellerDocument document1 = new TravellerDocument();
		document1.setDocumentType(DocumentType.ID_CARD);
		document1.setDocumentNumber("ABC123");
		document1.setIssuingCountry("Austria");

		TravellerDocument document2 = new TravellerDocument();
		document2.setDocumentType(DocumentType.DRIVER_LICENSE);
		document2.setDocumentNumber("SWE123");
		document2.setIssuingCountry("Canada");

		return List.of(document1, document2);
	}
	private Traveller createSampleTraveller1() {
        Traveller traveller = new Traveller();
        traveller.setId(1L);
        traveller.setFirstName("Krishna");
        traveller.setLastName("Priya");
        traveller.setDateOfBirth("1994-04-27");
        traveller.setEmail("krishnavaradha@example.com");
        traveller.setMobileNumber("1234567890");
        traveller.setActive(true);
        return traveller;
    }
}
