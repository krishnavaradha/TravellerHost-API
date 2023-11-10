package com.development.travellerhost.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.PrintWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import com.development.traveller.customexception.DuplicateResourceException;
import com.development.traveller.customexception.TravellerAlreadyDeactivatedException;
import com.development.traveller.customexception.TravellerNotFoundException;
import com.development.travellerhost.dao.TravellerDocumentRepository;
import com.development.travellerhost.dao.TravellerRepository;
import com.development.travellerhost.model.DocumentType;
import com.development.travellerhost.model.Traveller;
import com.development.travellerhost.model.TravellerDocument;

import jakarta.transaction.Transactional;

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
			Optional<Traveller> existingTraveller = travellerRepository.findByEmailAndMobileNumber(traveller.getEmail(),
					traveller.getMobileNumber());

			if (existingTraveller.isPresent()) {
				Traveller existing = existingTraveller.get();
				if (!isCombinationUnique(traveller)) {
					throw new DuplicateResourceException(
							"Email, Mobile Number, and Document combination is not unique.");
				}
				updateExistingTraveller(existing, traveller.getDocuments());
				Traveller savedTraveller = travellerRepository.save(existing);
				// Add the new documents to the existing traveler's documents list
				traveller.getDocuments().forEach(document -> document.setTraveller(savedTraveller));
				travellerDocumentRepository.saveAll(traveller.getDocuments());

				// Return the saved traveler
				return savedTraveller;

			} else {

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
		} catch (DataIntegrityViolationException ex) {
			// Catch the specific exception for unique constraint violation
			ex.printStackTrace(System.out);
			throw new DuplicateResourceException(
					"Mobile number/Email Id already exists. Please use a different mobile number or Email Id.");
		} catch (Exception ex) {
			throw new RuntimeException("Failed to create traveler: " + ex.getMessage());
		}
	}

	private void updateExistingTraveller(Traveller existing, List<TravellerDocument> newDocuments) {
		existing.setFirstName(existing.getFirstName());
		existing.setLastName(existing.getLastName());
		existing.setDateOfBirth(existing.getDateOfBirth());

		// Preserve existing documents and add new documents
		List<TravellerDocument> allDocuments = new ArrayList<>();
		if (existing.getDocuments() != null) {
			allDocuments.addAll(existing.getDocuments());
		}
		allDocuments.addAll(newDocuments);
		existing.setDocuments(allDocuments);

		// If there's only one document and none of them are marked as active, set the
		// first one as active
		if (allDocuments.size() == 1 && !isAtLeastOneDocumentActive(allDocuments)) {
			allDocuments.iterator().next().setActive(true);
		} else if (isAtLeastOneDocumentActive(allDocuments)) {
			deactivateOtherDocumentsForTraveler(existing, allDocuments);
		} else {
			markAtLeastOneDocumentAsActive(allDocuments);
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

	private boolean isCombinationUnique(Traveller traveller) {
		Set<String> documentCombinations = new HashSet<>();

		for (TravellerDocument document : traveller.getDocuments()) {
			String combination = document.getDocumentType() + "-" + document.getDocumentNumber();

			if (!documentCombinations.add(combination)) {
				return false; // Duplicate document combination found
			}

			if (travellerRepository.existsByEmailAndMobileNumberAndDocumentsId(traveller.getEmail(),
					traveller.getMobileNumber(), document.getId())) {
				return false; // Duplicate email, mobile number, and document combination found
			}
		}

		return true;
	}

	@Override
	public Traveller searchActiveTravellers(String email, String mobile, DocumentType documentType,
			String documentNumber, String issuingCountry) {
		List<TravellerDocument> filteredResult = new ArrayList<>();
		if (StringUtils.isAllBlank(email, mobile, documentNumber, issuingCountry) && documentType == null) {
			throw new IllegalArgumentException("At least one search criteria must be provided.");

		}

		// Perform the search for active travelers
		Traveller traveller = travellerRepository.searchActiveTravellers(email, mobile, documentType, documentNumber,
				issuingCountry);
		System.out.println("search traveller " + traveller);
		if (traveller == null || (traveller != null && !traveller.getActive())) {
			throw new TravellerNotFoundException("Traveller Account is Deactivated or Not Found");
		}

		boolean isActiveTraveller = traveller.isActive();

		for (TravellerDocument document : traveller.getDocuments()) {
			if (isActiveTraveller && document.isActive()) {
				filteredResult.add(document);
			}

		}
		traveller.setDocuments(filteredResult);
		return traveller;

	}

	@Override
	@Transactional
	public Traveller deactivateTraveller(String firstName, String lastName, String dateOfBirth, String email,
			String mobileNumber) throws TravellerAlreadyDeactivatedException {
		if (email == null || mobileNumber == null) {
			throw new IllegalArgumentException("Email and Mobile Number is mandatory");
		}

		List<Traveller> travellers = travellerRepository.findByFirstNameOrLastNameOrDateOfBirthOrEmailAndMobileNumber(
				firstName, lastName, dateOfBirth, email, mobileNumber);

		if (travellers.isEmpty()) {
			throw new TravellerNotFoundException("Traveller not found with the given information");
		}

		Traveller traveller = travellers.get(0);

		if (!traveller.isActive()) {
			throw new TravellerAlreadyDeactivatedException("Traveller is already deactivated");
		}

		traveller.setActive(false);
		return travellerRepository.save(traveller);
	}

}
