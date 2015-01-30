/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package com.anaplan.connector.utils;

/**
 * Simple Enum used for sending different response types.
 *
 * @author spondonsaha
 */
public enum OperationStatus {

	SUCCESS,
	FAILURE,
	APPLICATION_ERROR;

	private OperationStatus() {}
}