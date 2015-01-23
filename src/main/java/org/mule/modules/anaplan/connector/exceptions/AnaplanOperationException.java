/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package org.mule.modules.anaplan.connector.exceptions;


/**
 * Used for throwing upsert, export, delete operations.
 * @author spondonsaha
 */
public class AnaplanOperationException extends Exception{

	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception with the specified message.
	 * @param message
	 */
	public AnaplanOperationException(String message) {
		super(message);
	}

	/**
	 * Creates an exception with the specified message and throwable to throw.
	 * @param message
	 * @param cause
	 */
	public AnaplanOperationException(String message, Throwable cause) {
		super(message, cause);
	}
}
