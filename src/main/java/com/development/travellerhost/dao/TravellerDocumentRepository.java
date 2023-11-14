package com.development.travellerhost.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.development.travellerhost.model.DocumentType;
import com.development.travellerhost.model.Traveller;
import com.development.travellerhost.model.TravellerDocument;

@Repository
public interface TravellerDocumentRepository extends JpaRepository<TravellerDocument, Long> {
	 
		boolean existsByDocumentTypeAndDocumentNumberAndIssuingCountry(DocumentType documentType, String documentNumber,
				String issuingCountry);

		boolean existsByDocumentTypeAndDocumentNumberAndIssuingCountryAndTraveller(DocumentType documentType,
				String documentNumber, String issuingCountry, Traveller traveller);
	}