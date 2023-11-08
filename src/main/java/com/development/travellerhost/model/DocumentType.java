package com.development.travellerhost.model;
import com.development.traveller.customexception.InvalidDocumentTypeException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum DocumentType {
	PASSPORT("Passport"),
    ID_CARD("ID Card"),
    DRIVER_LICENSE("Driver's License");

    @JsonCreator
    public static DocumentType fromValue(String value) {
        try {
            return DocumentType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
           throw new InvalidDocumentTypeException("DocumentType is invalid , Has to be Passport or Id Card or Driving License");
        }
    }
    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
