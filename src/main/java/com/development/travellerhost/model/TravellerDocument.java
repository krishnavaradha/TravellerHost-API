package com.development.travellerhost.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@Entity
@Table(name = "TRAVELLER_DOCUMENTS")
public class TravellerDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
    private String documentNumber;
    private String issuingCountry;
    private boolean active;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "traveller_id")
    private Traveller traveller;

	public TravellerDocument(DocumentType documentType, String documentNumber, String issuingCountry) {
		super();
		this.documentType = documentType;
		this.documentNumber = documentNumber;
		this.issuingCountry = issuingCountry;
	}	
}

