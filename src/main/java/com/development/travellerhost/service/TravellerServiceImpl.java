package com.development.travellerhost.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.development.traveller.customexception.DuplicateResourceException;
import com.development.traveller.customexception.TravellerAlreadyDeactivatedException;
import com.development.traveller.customexception.TravellerAlredyExistsException;
import com.development.traveller.customexception.TravellerNotFoundException;
import com.development.travellerhost.dao.TravellerDocumentRepository;
import com.development.travellerhost.dao.TravellerRepository;
import com.development.travellerhost.model.DocumentType;
import com.development.travellerhost.model.Traveller;
import com.development.travellerhost.model.TravellerDocument;

import jakarta.transaction.Transactional;

@Service
@Transactional
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
	public Traveller createTraveller(Traveller traveller) throws TravellerAlredyExistsException {
		try {
			Optional<Traveller> existingTraveller = travellerRepository.findByEmailAndMobileNumber(traveller.getEmail(),
					traveller.getMobileNumber());
			if (existingTraveller.isPresent()) {
				throw new TravellerAlredyExistsException("Traveller already exists. Failed to create new traveller.");
			} else {

				checkForDuplicateDocuments(traveller);

				handleDocumentActivation(traveller);

				traveller.getDocuments().forEach(document -> document.setTraveller(traveller));
				Traveller savedTraveller = travellerRepository.save(traveller);

				return savedTraveller;
			}
		} catch (Exception ex) {
			if (ex instanceof DataIntegrityViolationException) {
				throw new DataIntegrityViolationException(
						"Mobile number/Email Id already exists. Please use a different mobile number or Email Id.");
			} else {
				throw new RuntimeException("Failed to create traveller: " + ex.getMessage(), ex);
			}
		}
	}

	private void checkForDuplicateDocuments(Traveller traveller) throws DuplicateResourceException {
		Set<String> documentCombinations = new HashSet<>();

		for (TravellerDocument document : traveller.getDocuments()) {
			String combination = document.getDocumentType() + "_" + document.getDocumentNumber() + "_"
					+ document.getIssuingCountry();

			if (!documentCombinations.add(combination)) {
				throw new DuplicateResourceException("Duplicate document combination found in the request.");
			}
			boolean uniqueDocumentCombination = !travellerDocumentRepository
					.existsByDocumentTypeAndDocumentNumberAndIssuingCountry(document.getDocumentType(),
							document.getDocumentNumber(), document.getIssuingCountry());

			if (!uniqueDocumentCombination) {
				// Duplicate document combination found for another traveler
				throw new DuplicateResourceException("Duplicate document combination found.");
			}

		}

	}

	private void updateExistingTraveller(Traveller existing, List<TravellerDocument> newDocuments)
			throws DuplicateResourceException {

		List<TravellerDocument> updatedDocuments = new ArrayList<>(existing.getDocuments());
		if (!isCombinationUnique(existing, newDocuments)) {
			throw new DuplicateResourceException(
					" Documents combination is not unique in the request.It is alredy assigned to the same traveller.");
		}
		for (TravellerDocument newDocument : newDocuments) {
			Optional<TravellerDocument> existingDocumentOpt = existing.getDocuments().stream()
					.filter(doc -> doc.getDocumentType().equals(newDocument.getDocumentType())
							&& doc.getDocumentNumber().equals(newDocument.getDocumentNumber())
							&& doc.getIssuingCountry().equals(newDocument.getIssuingCountry()))
					.findFirst();

			if (existingDocumentOpt.isPresent()) {
				// Update properties of existing document
				TravellerDocument existingDocument = existingDocumentOpt.get();
				existingDocument.setDocumentType(newDocument.getDocumentType());
				existingDocument.setDocumentNumber(newDocument.getDocumentNumber());
				existingDocument.setIssuingCountry(newDocument.getIssuingCountry());
				existingDocument.setActive(newDocument.isActive());
			} else {
				// Add new document with correct traveler reference
				newDocument.setTraveller(existing);
				updatedDocuments.add(newDocument);
			}
		}
		// Set the updated documents
		existing.setDocuments(updatedDocuments);

		// If there's only one document and none of them are marked as active, set the
		// first one as active
		if (updatedDocuments.size() == 1 && !isAtLeastOneDocumentActive(updatedDocuments)) {
			updatedDocuments.iterator().next().setActive(true);
		} else if (isAtLeastOneDocumentActive(updatedDocuments)) {
			deactivateOtherDocumentsForTraveler(existing, updatedDocuments);
		} else {
			markAtLeastOneDocumentAsActive(updatedDocuments);
		}
	}

	private void handleDocumentActivation(Traveller traveller) throws DuplicateResourceException {
		if (traveller.getDocuments().size() == 1) {
			traveller.getDocuments().iterator().next().setActive(true);
		} else {
			markAtLeastOneDocumentAsActive(traveller.getDocuments());
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

	private boolean isCombinationUnique(Traveller traveller, List<TravellerDocument> documents) {
		for (TravellerDocument document : documents) {

			// Check if the same combination is assigned to the same traveler
			boolean sameCombinationAssignedToSameTraveller = travellerDocumentRepository
					.existsByDocumentTypeAndDocumentNumberAndIssuingCountryAndTraveller(document.getDocumentType(),
							document.getDocumentNumber(), document.getIssuingCountry(), traveller);

			if (sameCombinationAssignedToSameTraveller) {
				// Same document combination is already assigned to the same traveler
				return false;
			}
		}

		return true;
	}

	@Override
	public Traveller searchActiveTravellers(String email, String mobile, DocumentType documentType,
			String documentNumber, String issuingCountry) throws TravellerNotFoundException {

		if (StringUtils.isAllBlank(email, mobile, documentNumber, issuingCountry) && documentType == null) {
			throw new IllegalArgumentException(
					"Email/Mobile/Document Deatils,At least one search criteria must be provided.");

		}
		// Perform the search for active travelers
		Traveller traveller = travellerRepository.searchActiveTravellers(email, mobile, documentType, documentNumber,
				issuingCountry);
		if (traveller == null || (traveller != null && !traveller.getActive())) {
			throw new TravellerNotFoundException("Traveller Account is Deactivated or Not Found");
		}
		return traveller;
	}

	@Override
	public Traveller deactivateTraveller(String firstName, String lastName, String dateOfBirth, String email,
			String mobileNumber) throws TravellerAlreadyDeactivatedException, TravellerNotFoundException {

		if (email == null || mobileNumber == null) {
			throw new IllegalArgumentException("Email or  Mobile Number is mandatory");
		}

		Traveller traveller = travellerRepository.findByEmailAndMobileNumberAndFirstNameAndLastNameAndDateOfBirth(email,
				mobileNumber, firstName, lastName, dateOfBirth);
		if (traveller == null) {
			throw new TravellerNotFoundException("Traveller not found with the given information");
		}

		checkIfAlreadyDeactivated(traveller);

		traveller.setActive(false);
		return travellerRepository.save(traveller);
	}

	// method to check if a traveler is already deactivated
	private void checkIfAlreadyDeactivated(Traveller traveller) throws TravellerAlreadyDeactivatedException {
		if (!traveller.getActive()) {
			throw new TravellerAlreadyDeactivatedException("Traveller is already deactivated");
		}
	}

	@Override
	public Traveller updateTraveller(Traveller newTraveller)
			throws TravellerAlreadyDeactivatedException, DuplicateResourceException, TravellerNotFoundException {
		checkForDuplicateDocuments(newTraveller);
		return travellerRepository.findByEmailAndMobileNumber(newTraveller.getEmail(), newTraveller.getMobileNumber())
				.map(existing -> {
					if (!existing.getActive()) {
						throw new RuntimeException("Deactivated travelers cannot be updated.");
					}

					existing.setFirstName(newTraveller.getFirstName());
					existing.setLastName(newTraveller.getLastName());
					existing.setDateOfBirth(newTraveller.getDateOfBirth());

					try {
						updateExistingTraveller(existing, newTraveller.getDocuments());
					} catch (DuplicateResourceException e) {
						throw new RuntimeException(e.getMessage());
					}

					Traveller savedTraveller = travellerRepository.save(existing);

					if (newTraveller.getDocuments() != null) {
						newTraveller.getDocuments().forEach(document -> document.setTraveller(savedTraveller));
						travellerDocumentRepository.saveAll(newTraveller.getDocuments());
					}

					return savedTraveller;
				}).orElseThrow(() -> new TravellerNotFoundException("Traveller not found with the provided details."));
	}

}