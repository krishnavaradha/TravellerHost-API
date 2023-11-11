package com.development.travellerhost.service;

import com.development.traveller.customexception.DuplicateResourceException;
import com.development.traveller.customexception.TravellerAlreadyDeactivatedException;
import com.development.traveller.customexception.TravellerAlredyExistsException;
import com.development.traveller.customexception.TravellerNotFoundException;
import com.development.travellerhost.model.DocumentType;
import com.development.travellerhost.model.Traveller;


	public interface TravellerService {
	    
		Traveller createTraveller(Traveller traveller) throws TravellerAlredyExistsException;

		Traveller searchActiveTravellers(String email, String mobile, DocumentType documentType,
				String documentNumber, String issuingCountry) throws TravellerAlreadyDeactivatedException, TravellerNotFoundException;

		Traveller deactivateTraveller(String firstName, String lastName, String dateOfBirth, String email,
				String mobileNumber) throws TravellerAlreadyDeactivatedException, TravellerNotFoundException;

		Traveller updateTraveller(Traveller traveller) throws TravellerAlreadyDeactivatedException, DuplicateResourceException, TravellerNotFoundException;

		
	
	}