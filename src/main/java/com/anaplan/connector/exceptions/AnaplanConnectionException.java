/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package com.anaplan.connector.exceptions;


public class AnaplanConnectionException extends Exception {

	private static final long serialVersionUID = 1L;

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