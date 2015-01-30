/**
 * Copyright 2015 Anaplan Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License.md file for the specific language governing permissions and
 * limitations under the License.
 */

package com.anaplan.connector.exceptions;


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
