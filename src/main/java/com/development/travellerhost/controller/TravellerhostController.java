package com.development.travellerhost.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.development.travellerhost.model.Traveller;
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
	         } catch (Exception e) {
	             // Handle exceptions gracefully
	             // You can log the exception for debugging
	             // and return an appropriate error response
	             return new ResponseEntity<>("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	         }
	     }
	 }

