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

import org.mule.api.annotations.Connect;
import org.mule.api.annotations.TestConnectivity;
import org.mule.api.annotations.components.ConnectionManagement;
import org.mule.api.annotations.display.Path;
import org.mule.api.annotations.param.ConnectionKey;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;

import com.anaplan.connector.utils.LogUtil;


@ConnectionManagement(
		friendlyName="Certificate Authentication",
		configElementName="cert-auth-connection")
public class CertAuthConnectionStrategy extends BaseConnectionStrategy {

	/**
	 * Connect to the Anaplan API using an issued certificate. The certificate
	 * path is provided and the file is loaded into a Java Keystore and loaded
	 * into a X509Certificate object for use.
	 *
	 * @param certificatePath
	 * @param url
	 * @param proxyHost
	 * @param proxyUser
	 * @param proxyPass
	 * @throws org.mule.api.ConnectionException
	 */
	@Connect
	@TestConnectivity
	public synchronized void connect(
			@ConnectionKey @Path String certificatePath,
			@Default("https://api.anaplan.com/") String url,
			@Optional @Default("") String proxyHost,
			@Optional @Default("") String proxyUser,
			@Optional @Default("") String proxyPass)
					throws org.mule.api.ConnectionException {
		LogUtil.status(getClass().toString(),
				"Initiating certificate connection...");

		if (apiConn == null) {
			// create the connection strategy using certificate path.
			apiConn = new AnaplanConnection(true, certificatePath, url,
					proxyHost, proxyUser, proxyPass);
			// Establish connection using new connection object and verify
			// service parameters
			connectToApi();
		}
	}
}
