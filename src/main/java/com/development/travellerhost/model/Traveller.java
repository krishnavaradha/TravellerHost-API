package com.development.travellerhost.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class Traveller {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "active")
	private Boolean active=true;
	private String firstName;
	private String lastName;
	private String dateOfBirth;
	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false, unique = true)
	private String mobileNumber;

	@OneToMany(mappedBy = "traveller", cascade = CascadeType.ALL)
	private List<TravellerDocument> documents;
	

}