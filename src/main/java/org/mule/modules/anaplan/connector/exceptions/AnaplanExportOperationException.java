package org.mule.modules.anaplan.connector.exceptions;


/**
 * Used when errors are encountered during the Export process.
 * @author spondonsaha
 */
public class AnaplanExportOperationException extends Exception {

	/**
	 * Creates an exception with the specified message
	 * @param message
	 */
	public AnaplanExportOperationException(String message) {
		super(message);
	}

	/**
	 * Creates an exception with the specified message and cause.
	 * @param message
	 * @param cause
	 */
	public AnaplanExportOperationException(String message, Throwable cause) {
		super(message, cause);
	}
}
