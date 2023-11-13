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
				throw new TravellerAlredyExistsException("Traveller alredy exists, Failed to create traveller ");
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

			throw new TravellerAlredyExistsException(
					"Mobile number/Email Id already exists. Please use a different mobile number or Email Id.");
		} catch (Exception ex) {
			throw new RuntimeException("Failed to create traveler: " + ex.getMessage());
		}
	}

	private void updateExistingTraveller(Traveller existing, List<TravellerDocument> newDocuments) {
		List<TravellerDocument> updatedDocuments = new ArrayList<>(existing.getDocuments());

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
	@Transactional()
	public Traveller searchActiveTravellers(String email, String mobile, DocumentType documentType,
			String documentNumber, String issuingCountry) throws TravellerNotFoundException {
		try {
			if (StringUtils.isAllBlank(email, mobile, documentNumber, issuingCountry) && documentType == null) {
				throw new IllegalArgumentException(
						"Email/Mobile/Document Deatils,At least one search criteria must be provided.");

			}
			// Perform the search for active travelers
			Traveller traveller = travellerRepository.searchActiveTravellers(email, mobile, documentType,
					documentNumber, issuingCountry);
			if (traveller == null || (traveller != null && !traveller.getActive())) {
				throw new TravellerNotFoundException("Traveller Account is Deactivated or Not Found");
			}
			return traveller;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to search traveller: " + ex.getMessage());
		}

	}

	@Override
	@Transactional
	public Traveller deactivateTraveller(String firstName, String lastName, String dateOfBirth, String email,
			String mobileNumber) throws TravellerAlreadyDeactivatedException, TravellerNotFoundException {
		try {
			if (email == null || mobileNumber == null) {
				throw new IllegalArgumentException("Email and Mobile Number is mandatory");
			}

			Traveller traveller = travellerRepository.findByEmailAndMobileNumberAndFirstNameAndLastNameAndDateOfBirth(
					email, mobileNumber, firstName, lastName, dateOfBirth);
			if (traveller == null) {
				throw new TravellerNotFoundException("Traveller not found with the given information");
			}

			if (!traveller.getActive()) {
				throw new TravellerAlreadyDeactivatedException("Traveller is already deactivated");
			}

			traveller.setActive(false);
			return travellerRepository.save(traveller);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to deactivate traveller: " + ex.getMessage());
		}
	}

	@Override
	@Transactional
	public Traveller updateTraveller(Traveller newTraveller)
			throws TravellerAlreadyDeactivatedException, DuplicateResourceException, TravellerNotFoundException {
		try {
			if (!isCombinationUnique(newTraveller)) {
				throw new DuplicateResourceException(
						"Email, Mobile Number, and Document combination is not unique in the request.");
			}

			return travellerRepository
					.findByEmailAndMobileNumber(newTraveller.getEmail(), newTraveller.getMobileNumber())
					.map(existing -> {
						if (!existing.getActive()) {
							throw new RuntimeException("Deactivated travelers cannot be updated.");
						}

						existing.setFirstName(newTraveller.getFirstName());
						existing.setLastName(newTraveller.getLastName());
						existing.setDateOfBirth(newTraveller.getDateOfBirth());

						updateExistingTraveller(existing, newTraveller.getDocuments());

						Traveller savedTraveller = travellerRepository.save(existing);

						if (newTraveller.getDocuments() != null) {
							newTraveller.getDocuments().forEach(document -> document.setTraveller(savedTraveller));
							travellerDocumentRepository.saveAll(newTraveller.getDocuments());
						}

						return savedTraveller;
					}).orElseThrow(
							() -> new TravellerNotFoundException("Traveller not found with the provided details."));
		} catch (Exception ex) {
			throw new RuntimeException("Failed to update traveller: " + ex.getMessage());
		}
	}

}