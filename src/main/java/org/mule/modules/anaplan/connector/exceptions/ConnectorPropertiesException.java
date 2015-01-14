package org.mule.modules.anaplan.connector.exceptions;

public class ConnectorPropertiesException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception with the specified messsage
	 * @param message
	 */
	public ConnectorPropertiesException(String message) {
		super(message);
	}

	/**
	 * Creates an exception with the specified message and cause
	 * @param message
	 * @param cause
	 */
	public ConnectorPropertiesException(String message, Throwable cause) {
		super(message, cause);
	}
}