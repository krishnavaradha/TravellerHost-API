package com.development.travellerhost.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public List<TravellerDocument> getDocuments() {
		return documents;
	}

	public void setDocuments(List<TravellerDocument> documents) {
		this.documents = documents;
	}

	public boolean isActive() {
		
		return active;
	}


}