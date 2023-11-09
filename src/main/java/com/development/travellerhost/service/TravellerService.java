package com.development.travellerhost.service;

import java.util.List;
import java.util.Optional;

import com.development.travellerhost.model.DocumentType;
import com.development.travellerhost.model.Traveller;
import com.development.travellerhost.model.TravellerDocument;


	public interface TravellerService {
	    
		Traveller createTraveller(Traveller traveller);

		List<Traveller> searchActiveTravellers(String email, String mobile, DocumentType documentType,
				String documentNumber, String issuingCountry);
	
	}