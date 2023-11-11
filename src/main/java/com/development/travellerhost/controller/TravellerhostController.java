package com.development.travellerhost.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.development.traveller.customexception.TravellerAlreadyDeactivatedException;
import com.development.traveller.customexception.TravellerAlredyExistsException;
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
		}  catch (TravellerAlredyExistsException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}catch (Exception e) {
			return new ResponseEntity<>("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
	    } catch (TravellerNotFoundException | TravellerAlreadyDeactivatedException e) {
	        return handleException(e);
	    } catch (Exception e) {
	        return handleException(new RuntimeException("An error occurred: " + e.getMessage(), e));
	    }
	}

	@PutMapping("/update")
	public ResponseEntity<?> updateTraveller(@RequestBody Traveller traveller) {
	    try {
	        Traveller updatedTraveller = travellerService.updateTraveller(traveller);
	        return new ResponseEntity<>(updatedTraveller, HttpStatus.OK);
	    } catch (TravellerNotFoundException | TravellerAlreadyDeactivatedException e) {
	        return handleException(e);
	    } catch (Exception e) {
	        return handleException(new RuntimeException("An error occurred: " + e.getMessage(), e));
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
	            return ResponseEntity.notFound().build();
	        }
	    } catch (TravellerNotFoundException | TravellerAlreadyDeactivatedException e) {
	        return handleException(e);
	    } catch (Exception e) {
	        return handleException(new RuntimeException("An error occurred: " + e.getMessage(), e));
	    }
	}
	private ResponseEntity<?> handleException(Exception e) {
	    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
	    if (e instanceof TravellerNotFoundException) {
	        status = HttpStatus.NOT_FOUND;
	    } else if (e instanceof TravellerAlreadyDeactivatedException) {
	        status = HttpStatus.BAD_REQUEST;
	    }
	    return ResponseEntity.status(status).body(e.getMessage());
	}
}
