package com.development.travellerhost.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
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
}
