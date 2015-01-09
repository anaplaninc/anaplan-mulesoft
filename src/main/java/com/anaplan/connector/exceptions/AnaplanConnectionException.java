package com.anaplan.connector.exceptions;

public class AnaplanConnectionException extends Exception {
	
	/**
	 * Creates an exception with the specified messsage
	 * @param message
	 */
	public AnaplanConnectionException(String message) {
		super(message);
	}
	
	/**
	 * Creates an exception with the specified message and cause
	 * @param message
	 * @param cause
	 */
	public AnaplanConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
}