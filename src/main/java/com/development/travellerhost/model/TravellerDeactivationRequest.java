package com.development.travellerhost.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class TravellerDeactivationRequest {
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    @NotNull
    @NotEmpty(message = "Email cannot be empty")
    private String email;
    @NotNull
    @NotEmpty(message = "MobileNumber cannot be empty")
    private String mobileNumber;
	
	public TravellerDeactivationRequest(String firstName, String lastName, String dateOfBirth, String email,
			String mobileNumber) {	
		this.firstName = firstName;
		this.lastName = lastName;
		this.dateOfBirth = dateOfBirth;
		this.email = email;
		this.mobileNumber = mobileNumber;
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

}
