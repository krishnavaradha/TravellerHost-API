package com.development.travellerhost.model;

public class TravellerDocumentRequest {
    private Traveller traveller;
    private TravellerDocument document;
	public Traveller getTraveler() {
		return traveller;
	}
	public void setTraveler(Traveller traveler) {
		this.traveller = traveler;
	}
	public TravellerDocument getDocument() {
		return document;
	}
	public void setDocument(TravellerDocument document) {
		this.document = document;
	}
}