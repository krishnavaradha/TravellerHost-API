package com.development.traveller.customexception;

public class InvalidDocumentTypeException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidDocumentTypeException() {
        super();
    }

    public InvalidDocumentTypeException(String message) {
        super(message);
    }

    public InvalidDocumentTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDocumentTypeException(Throwable cause) {
        super(cause);
    }
}

