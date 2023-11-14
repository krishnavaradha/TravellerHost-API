package com.development.travellerhost.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.development.traveller.customexception.DuplicateResourceException;
import com.development.traveller.customexception.ErrorResponse;
import com.development.traveller.customexception.TravellerAlreadyDeactivatedException;
import com.development.traveller.customexception.TravellerNotFoundException;
import com.development.travellerhost.model.DocumentType;
import com.development.travellerhost.model.Traveller;
import com.development.travellerhost.model.TravellerDeactivationRequest;
import com.development.travellerhost.model.TravellerDocument;
import com.development.travellerhost.service.TravellerService;

@RestController
@RequestMapping("/api/travellers")
public class TravellerhostController {
	private final TravellerService travellerService;

	@Autowired
	public TravellerhostController(TravellerService travellerService) {
		this.travellerService = travellerService;
	}

	@PostMapping("/create")
	public ResponseEntity<?> createTraveller(@RequestBody Traveller traveller) {
		try {
			Traveller createdTraveller = travellerService.createTraveller(traveller);
			return new ResponseEntity<>(createdTraveller, HttpStatus.CREATED);
		}
		catch (Exception e) {
			 return handleException(e);
		}
	}

	@GetMapping("/search")
	public ResponseEntity<?> searchTravellers(
	    @RequestParam(required = false) String email,
	    @RequestParam(required = false) String mobile,
	    @RequestParam(required = false) DocumentType documentType,
	    @RequestParam(required = false) String documentNumber,
	    @RequestParam(required = false) String issuingCountry) {

	    try {
	        TravellerDocument document = new TravellerDocument(documentType, documentNumber, issuingCountry);
	        Traveller traveller = travellerService.searchActiveTravellers(
	            email, mobile, document.getDocumentType(), document.getDocumentNumber(), document.getIssuingCountry());

	        return new ResponseEntity<>(traveller, HttpStatus.OK);
	    }catch (Exception e) {
	        return handleException(e);
	    }
	}

	@PutMapping("/update")
	public ResponseEntity<?> updateTraveller(@RequestBody Traveller traveller) {
	    try {
	        Traveller updatedTraveller = travellerService.updateTraveller(traveller);
	        return new ResponseEntity<>(updatedTraveller, HttpStatus.OK);
	    } catch (Exception e) {
	        return handleException(e);
	    }
	}

	@PutMapping("/deactivate")
	public ResponseEntity<?> deactivateTraveller(@RequestBody TravellerDeactivationRequest request) {
	    try {
	        Traveller deactivatedTraveller = travellerService.deactivateTraveller(
	            request.getFirstName(), request.getLastName(), request.getDateOfBirth(), request.getEmail(), request.getMobileNumber());

	        if (deactivatedTraveller != null) {
	            return new ResponseEntity<>(deactivatedTraveller, HttpStatus.OK);
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	        }
	    } catch (Exception e) {
	        return handleException(e);
	    }
	}
	private ResponseEntity<?> handleException(Exception e) {
	    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
	    String errorMessage = "An error occurred: " + e.getMessage();

	    if (e instanceof TravellerNotFoundException || e instanceof TravellerAlreadyDeactivatedException) {
	        status = HttpStatus.NOT_FOUND;
	        errorMessage = e.getMessage();  
	    } else if (e instanceof DataIntegrityViolationException) {
	        status = HttpStatus.BAD_REQUEST;
	        errorMessage = "Mobile number/Email Id already exists. Please use a different mobile number or Email Id.";        
	    }else if (e instanceof DuplicateResourceException) {
	        status = HttpStatus.CONFLICT;
	        errorMessage = e.getMessage();
	    }

	    return ResponseEntity.status(status).body(new ErrorResponse(status.value(), errorMessage));
	}
}
