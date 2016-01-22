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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mule.api.annotations.Connect;
import org.mule.api.annotations.TestConnectivity;
import org.mule.api.annotations.components.ConnectionManagement;
import org.mule.api.annotations.display.Password;
import org.mule.api.annotations.param.ConnectionKey;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;

/**
 * Basic connection strategy to authenticate the Anaplan user via provided
 * username and password. If behind proxy connection, then proxy credentials
 * are required.
 * @author spondonsaha
 *
 */
@ConnectionManagement(
		friendlyName="Basic Authentication",
		configElementName="basic-auth-connection")
public class BasicAuthConnectionStrategy extends BaseConnectionStrategy {

	private static Logger logger = LogManager.getLogger(
            BasicAuthConnectionStrategy.class.getName());

	/**
	 * Connect to the Anaplan API via basic authentication using provided
	 * username and password.
	 *
	 * @param username User's username to login to Anaplan API.
	 * @param password User's password to login to Anaplan API.
	 * @param url API URL.
	 * @param proxyHost Proxy URL if behind firewall.
	 * @param proxyUser Proxy username to get past firewall.
	 * @param proxyPass Proxy password to get past firewall.
	 * @throws org.mule.api.ConnectionException Whenever the connection to
	 * 		Anaplan API fails using provided credentials.
	 */
	@Connect
	@TestConnectivity
	public synchronized void connect(
			@ConnectionKey String username,
			@Password String password,
			@Default("https://api.anaplan.com/") String url,
			@Optional @Default("") String proxyHost,
			@Optional @Default("") String proxyUser,
			@Optional @Password @Default("") String proxyPass)
					throws org.mule.api.ConnectionException {

		logger.info("Initiating basic connection...");

		if (apiConn == null) {
			// create the connStrategy object using credentials provided.
			apiConn = new AnaplanConnection(false, username, password, url,
					proxyHost, proxyUser, proxyPass);
			// Establish connection using new connection object and verify
			// service parameters
			connectToApi();
		}
	}
}
