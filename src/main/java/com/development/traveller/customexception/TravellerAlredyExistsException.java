package com.development.traveller.customexception;

public class TravellerAlredyExistsException extends Exception{
    public TravellerAlredyExistsException(String message, Exception ex) {
        super(message);
    }
    public TravellerAlredyExistsException(String message) {
        super(message);
    }
}
