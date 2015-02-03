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

package com.anaplan.connector.connection;

import org.mule.api.ConnectionException;
import org.mule.api.ConnectionExceptionCode;
import org.mule.api.annotations.ConnectionIdentifier;
import org.mule.api.annotations.Disconnect;
import org.mule.api.annotations.ValidateConnection;

import com.anaplan.client.Service;
import com.anaplan.connector.exceptions.AnaplanConnectionException;
import com.anaplan.connector.utils.LogUtil;


/**
 * Base abstract connection strategy class.
 * @author spondonsaha
 */
public class BaseConnectionStrategy {

	protected AnaplanConnection apiConn;

	/**
	 * Getter for the AnaplanConnection object, initiated using basic auth
	 * credentials or certificate.
	 * @return
	 */
	public AnaplanConnection getService() {
		return apiConn;
	}

	/**
	 * Disconnect
	 */
	@Disconnect
	public void disconnect() {
		if (apiConn != null) {
			apiConn.closeConnection();
		} else {
			LogUtil.error(getClass().toString(), "No connStrategy to disconnect!");
		}
	}

	/**
	 * Are we connected?
	 */
	@ValidateConnection
	public boolean isConnected() {
		return apiConn != null;
	}

	/**
	 * Are we connected?
	 */
	@ConnectionIdentifier
	public String connectionId() {
		if (apiConn != null)
			return apiConn.getConnectionId();
		else
			return "Not connected!";
	}

	/**
	 * Opens the API connection and validates the service, else throws
	 * a {@link ConnectionException} for Mulesoft.
	 *
	 * @throws ConnectionException
	 */
	protected synchronized void connectToApi() throws ConnectionException {
		Service service = null;
		// Connect to the Anaplan API.
		try {
			service = apiConn.openConnection();
		} catch (AnaplanConnectionException e) {
			throw new org.mule.api.ConnectionException(
					ConnectionExceptionCode.INCORRECT_CREDENTIALS, null,
					e.getMessage(), e);
		}

		if (service == null) {
			throw new org.mule.api.ConnectionException(
					ConnectionExceptionCode.UNKNOWN, null, "No service "
					+ "object acquired after opening connStrategy to Anaplan "
					+ "API!", null);
		} else {
			LogUtil.status(getClass().toString(),
					"Successfully connected to Anaplan API!");
		}
	}

	/**
	 * Checks if the API connStrategy has been established. If no connStrategy
	 * object was found, then throws {@link AnaplanConnectionException}, else
	 * opens the connStrategy and registers the service object.
	 *
	 * @throws AnaplanConnectionException
	 */
	public void validateConnection() throws AnaplanConnectionException {
		// validate API connStrategy
		if (isConnected()) {
			if (apiConn.getConnection() == null) {
				apiConn.openConnection();
			} else {
				LogUtil.status(apiConn.getLogContext(),
						"Connection to API exists. Proceeding...");
			}
		} else {
			throw new AnaplanConnectionException(
					"No connStrategy object: call connect()");
		}
	}
}
