package com.development.travellerhost.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.development.traveller.customexception.DuplicateResourceException;
import com.development.travellerhost.dao.TravellerDocumentRepository;
import com.development.travellerhost.dao.TravellerRepository;
import com.development.travellerhost.model.Traveller;
import com.development.travellerhost.model.TravellerDocument;

@Service
public class TravellerServiceImpl implements TravellerService {

	private final TravellerRepository travellerRepository;
	private final TravellerDocumentRepository travellerDocumentRepository;

	@Autowired
	public TravellerServiceImpl(TravellerRepository travellerRepository,
			TravellerDocumentRepository travellerDocumentRepository) {
		this.travellerRepository = travellerRepository;
		this.travellerDocumentRepository = travellerDocumentRepository;
	}

	@Override
	public Traveller createTraveller(Traveller traveller) {
		Traveller createdTraveler = null;
		try {
			  Optional<Traveller> existingTraveller = travellerRepository.findByEmailAndMobileNumber(
			            traveller.getEmail(), traveller.getMobileNumber()
			        );
			  if (existingTraveller.isPresent()) {
		            // Traveler already exists, update the existing traveler
		            Traveller existing = existingTraveller.get();
		            // Update other fields as needed
		            existing.setFirstName(traveller.getFirstName());
		            existing.setLastName(traveller.getLastName());
		            existing.setDateOfBirth(traveller.getDateOfBirth());
		            existing.setActive(traveller.isActive());
		            // Set the existing traveler for each document
		            for (TravellerDocument document : traveller.getDocuments()) {
		                document.setTraveller(existing);
		            }
		         // Add the new documents to the existing traveler's documents list
		            if (traveller.getDocuments() != null) {
		                existing.getDocuments().addAll(traveller.getDocuments());
		            }
		            // Validate the combination of Email, Mobile Number, and Document
		            if (!isCombinationUnique(existing, traveller.getDocuments())) {
		                throw new DuplicateResourceException("Email, Mobile Number, and Document combination is not unique.");
		            }

		            // Save the updated traveler
		            traveller = travellerRepository.save(existing);
		            
		            // Deactivate other documents for the existing traveler
		           // deactivateOtherDocumentsForTraveler(existing, traveller.getDocuments());
		        } else {
		        	// Validate the combination of Email, Mobile Number, and Document
		            if (!isCombinationUnique(traveller, traveller.getDocuments())) {
		                throw new DuplicateResourceException("Email, Mobile Number, and Document combination is not unique.");
		            }
		            // Traveler doesn't exist, create a new traveler
		            traveller = travellerRepository.save(traveller);
		        }


		         // Add documents to the traveler
		            if (traveller.getDocuments() != null && !traveller.getDocuments().isEmpty()) {
		            	boolean isAtLeastOneDocumentActive = false;

		                for (TravellerDocument document : traveller.getDocuments()) {
		                	 //validateDocument(document, traveller);
		                    document.setTraveller(traveller);
		                    // If this document is marked as active, deactivate other documents
		                    if (document.isActive()) {
		                        if (isAtLeastOneDocumentActive) {
		                            // Set any other active documents to inactive
		                            deactivateOtherDocumentsForTraveler(traveller, document);
		                        }
		                        isAtLeastOneDocumentActive = true;
		                    }
		                    travellerDocumentRepository.save(document);
		                }
		            }

		           

		            return createdTraveler;
		        }

		      

		    


        catch (Exception ex) {
            // Handle any exceptions that may occur during traveler creation
            throw new RuntimeException("Failed to create traveler: " + ex.getMessage());
        }
    }


	private void validateDocument(TravellerDocument document,Traveller traveler) {
	    // Check if a document with the same combination already exists for the same traveler
	    Optional<TravellerDocument> existingDocument = travellerDocumentRepository
	            .findByDocumentTypeAndDocumentNumberAndIssuingCountryAndTraveller(
	                document.getDocumentType(), document.getDocumentNumber(),
	                document.getIssuingCountry(), traveler);

	    if (existingDocument.isPresent()) {
	        throw new DuplicateResourceException("Document with the same combination already exists for this traveler.");
	    }
	}
	private void deactivateOtherDocumentsForTraveler(Traveller traveler, TravellerDocument activeDocument) {
	    for (TravellerDocument document : traveler.getDocuments()) {
	        if (document.equals(activeDocument)) {
	            document.setActive(true);
	        } else {
	            document.setActive(false);
	        }
	        travellerDocumentRepository.save(document);
	    }
	}
    private boolean isDuplicateEmailMobileAndDocuments(String email, String mobileNumber, List<Long> documentIds) {
        return travellerRepository.existsByEmailMobileAndDocuments(email, mobileNumber, documentIds);
    }
    private List<Long> getDocumentIds(Traveller traveller) {
        List<Long> documentIds = new ArrayList<>();
        if (traveller.getDocuments() != null) {
            for (TravellerDocument document : traveller.getDocuments()) {
                documentIds.add(document.getId());
            }
        }
        return documentIds;
    }
    private boolean isCombinationUnique(Traveller traveler, List<TravellerDocument> documents) {
        // Check if the combination of Email, Mobile Number, and Document is unique
        for (TravellerDocument document : documents) {
            if (travellerRepository.existsByEmailAndMobileNumberAndDocumentsId(
                traveler.getEmail(), traveler.getMobileNumber(), document.getId()
            )) {
                return false;
            }
        }
        return true;
    }
}
		