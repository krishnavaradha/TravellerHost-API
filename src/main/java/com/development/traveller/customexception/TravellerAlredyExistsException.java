package com.development.traveller.customexception;

public class TravellerAlredyExistsException extends RuntimeException{
    public TravellerAlredyExistsException(String message) {
        super(message);
    }
}
