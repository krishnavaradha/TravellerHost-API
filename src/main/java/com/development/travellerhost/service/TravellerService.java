package com.development.travellerhost.service;

import com.development.traveller.customexception.TravellerAlreadyDeactivatedException;
import com.development.travellerhost.model.DocumentType;
import com.development.travellerhost.model.Traveller;


	public interface TravellerService {
	    
		Traveller createTraveller(Traveller traveller);

		Traveller searchActiveTravellers(String email, String mobile, DocumentType documentType,
				String documentNumber, String issuingCountry) throws TravellerAlreadyDeactivatedException;

		Traveller deactivateTraveller(String firstName, String lastName, String dateOfBirth, String email,
				String mobileNumber) throws TravellerAlreadyDeactivatedException;

		Traveller updateTraveller(Traveller traveller) throws TravellerAlreadyDeactivatedException;

		
	
	}