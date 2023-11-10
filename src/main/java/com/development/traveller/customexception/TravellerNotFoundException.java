package com.development.traveller.customexception;

public class TravellerNotFoundException extends RuntimeException {
    public TravellerNotFoundException(String message) {
        super(message);
    }
}
