package com.development.travellerhost.service;

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
        try {
            Optional<Traveller> existingTraveller = travellerRepository
                    .findByEmailAndMobileNumber(traveller.getEmail(), traveller.getMobileNumber());

            if (existingTraveller.isPresent()) {
                Traveller existing = existingTraveller.get();
                updateExistingTraveller(existing, traveller.getDocuments());
                return travellerRepository.save(existing);
            } else {
                if (!isCombinationUnique(traveller, traveller.getDocuments())) {
                    throw new DuplicateResourceException("Email, Mobile Number, and Document combination is not unique.");
                }

                // Save the traveler to generate an ID
                Traveller savedTraveller = travellerRepository.save(traveller);

                // Add the new documents to the existing traveler's documents list
                traveller.getDocuments().forEach(document -> document.setTraveller(savedTraveller));

                // If there's only one document, mark it as active
                if (traveller.getDocuments().size() == 1) {
                    traveller.getDocuments().iterator().next().setActive(true);
                } else {
                    // If there are multiple documents, mark at least one as active
                    markAtLeastOneDocumentAsActive(traveller.getDocuments());
                }

                // Save the updated documents
                travellerDocumentRepository.saveAll(traveller.getDocuments());

                // Return the saved traveler
                return savedTraveller;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create traveler: " + ex.getMessage());
        }
    }

    private void updateExistingTraveller(Traveller existing, List<TravellerDocument> newDocuments) {
        existing.setFirstName(existing.getFirstName());
        existing.setLastName(existing.getLastName());
        existing.setDateOfBirth(existing.getDateOfBirth());
        existing.setDocuments(newDocuments);

        // If there's only one document and none of them are marked as active, set the first one as active
        if (newDocuments.size() == 1 && !isAtLeastOneDocumentActive(newDocuments)) {
            newDocuments.iterator().next().setActive(true);
        } else if (isAtLeastOneDocumentActive(newDocuments)) {
            deactivateOtherDocumentsForTraveler(existing, newDocuments);
        }
    }

    private boolean isAtLeastOneDocumentActive(List<TravellerDocument> documents) {
        return documents.stream().anyMatch(TravellerDocument::isActive);
    }

    private void markAtLeastOneDocumentAsActive(List<TravellerDocument> documents) {
        documents.get(0).setActive(true);
    }

    private void deactivateOtherDocumentsForTraveler(Traveller existing, List<TravellerDocument> newDocuments) {
        for (TravellerDocument document : newDocuments) {
            if (!document.isActive()) {
                document.setActive(false);
            }
        }
    }

    private boolean isDuplicateEmailMobileAndDocuments(String email, String mobileNumber, List<Long> documentIds) {
        return travellerRepository.existsByEmailMobileAndDocuments(email, mobileNumber, documentIds);
    }

    private boolean isCombinationUnique(Traveller traveler, List<TravellerDocument> documents) {
        for (TravellerDocument document : documents) {
            if (travellerRepository.existsByEmailAndMobileNumberAndDocumentsId(traveler.getEmail(),
                    traveler.getMobileNumber(), document.getId())) {
                return false;
            }
        }
        return true;
    }
}