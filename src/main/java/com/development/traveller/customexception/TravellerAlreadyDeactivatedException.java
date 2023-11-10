package com.development.traveller.customexception;

public class TravellerAlreadyDeactivatedException extends Exception {

    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public TravellerAlreadyDeactivatedException() {
	        super();
	    }

	    public TravellerAlreadyDeactivatedException(String message) {
	        super(message);
	    }

	    public TravellerAlreadyDeactivatedException(String message, Throwable cause) {
	        super(message, cause);
	    }

	    public TravellerAlreadyDeactivatedException(Throwable cause) {
	        super(cause);
	    }
	}

