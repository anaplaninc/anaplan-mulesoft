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

/**
 * Basic Anaplan Connection class that helps establish an API connection using
 * the provided credentials from the connector.
 *
 * Author: Spondon Saha
 */

package com.anaplan.connector.connection;

import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.Credentials;
import com.anaplan.client.Service;
import com.anaplan.client.Workspace;
import com.anaplan.connector.AnaplanConnectorProperties;
import com.anaplan.connector.exceptions.AnaplanConnectionException;
import com.anaplan.connector.exceptions.ConnectorPropertiesException;
import com.anaplan.connector.utils.LogUtil;
import com.anaplan.connector.utils.UserMessages;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;


/**
 * Validation and communication with the Anaplan model.
 *
 * This component has no individual access to the user-facing boomi logs: errors
 * reported here should be handled by the enclosing operation with the supplied
 * context from {@link #getLogContext()}.
 */
public class AnaplanConnection {

	private static final String USERNAME_FIELD = "username";
	private static final String PASSWORD_FIELD = "password";
	private static final String URL_FIELD = "url";
	private static final String CERT_PATH = "certPath";
	private static final String URL_PROXY = "proxyHost";
	private static final String URL_PROXY_USER = "proxyUser";
	private static final String URL_PROXY_PASS = "proxyPass";

	private final AnaplanConnectorProperties connectionConfig;
	private final boolean isCertificate;

	// cached Anaplan objects when a valid open connection exists, else null
	private Service openConnection = null;


	/**
	 * Constructor.
	 *
	 * @param isCertificate
	 * 				If set to true then just use certificate for authentication.
	 * 				If false, then we are using basic authentication and so just
	 * 				need username and password.
	 * @param credentials
	 * 				String[] of credential values, which includes the username,
	 * 				password, API url, certificate path and proxy URL and their
	 * 				credentials if using this connector behind a firewall.
	 */
	public AnaplanConnection(boolean isCertificate, String... credentials) {
		LogUtil.debug("NOTICE: ", credentials[0] + " @ " + credentials[2]);
		this.isCertificate = isCertificate;
		connectionConfig = new AnaplanConnectorProperties();
		try {
			if (isCertificate)
				connectionConfig.setProperties(credentials, CERT_PATH,
						URL_FIELD, URL_PROXY, URL_PROXY_USER, URL_PROXY_PASS);
			else
				connectionConfig.setProperties(credentials, USERNAME_FIELD,
						PASSWORD_FIELD, URL_FIELD, URL_PROXY, URL_PROXY_USER,
						URL_PROXY_PASS);
		} catch (ConnectorPropertiesException e) {
			LogUtil.error(getLogContext(),
					"Could not set connector properties!"
							+ e.toString());
		}
		LogUtil.status(getLogContext(), "Stored connection properties!");
	}

	/**
	 * Getter for the connection ID, which is the string representation of this
	 * object.
	 *
	 * @return
	 */
	public String getConnectionId() {
		return this.toString();
	}

	/**
	 * Opens the DER encoded certificate from the provided path into an input
	 * stream and then generates a X.509 certificate from the file contents and
	 * returns it. To view the certificate contents on the command line, do:
	 * $ openssl x509 -in /path/to/certificate/file.cer -inform der -text -noout
	 *
	 * @param certificateLocation, X509 certificate location on local.
	 * @return X509 certificate object.
	 * @throws AnaplanConnectionException
	 */
	public X509Certificate readCertificate(String certificateLocation)
					throws AnaplanConnectionException {
		BufferedInputStream buffStream = null;
		X509Certificate x509 = null;
		try {
			buffStream = new BufferedInputStream(
					new FileInputStream(certificateLocation));
			Certificate cert = CertificateFactory.getInstance("X.509")
					.generateCertificate(buffStream);
			if (cert instanceof X509Certificate) {
				x509 = (X509Certificate) cert;
				LogUtil.status(getLogContext(), "Certificate VALID!");
				LogUtil.debug(getLogContext(), x509.toString());
			}
		} catch (CertificateException e) {
			throw new AnaplanConnectionException(
					"Bad certificate: " + e.getMessage());
        } catch (IOException e) {
        	throw new AnaplanConnectionException(
        			"Could not open certificate: " + e.getMessage());
        } catch (Throwable e) {
			throw new AnaplanConnectionException(
					"Unknown exception occured: " + e.getMessage());
        } finally {
            if (buffStream != null) {
                try {
                	buffStream.close();
				} catch (IOException e) {
					throw new AnaplanConnectionException(e.getMessage());
				}
            }
        }
		return x509;
	}

	/**
	 * Opens a service for accessing anaplan workspaces with this connection's
	 * API endpoint and credentials.
	 *
	 * @return The service object that contains workspace/model details for the
	 *         authenticated user.
	 *
	 * @throws AnaplanConnectionException
	 *             If there was an error with the service or any or the required
	 *             properties.
	 */
	private Service cacheService() throws AnaplanConnectionException {
		LogUtil.debug(getLogContext(), "trying Anaplan service connection...");

		final String apiUrl = connectionConfig.getStringProperty(URL_FIELD);
		Service service = null;
		LogUtil.warning(getLogContext(), "API Url: " + apiUrl);
		try {
			service = new Service(new URI(apiUrl));
		} catch (URISyntaxException e) {
			closeConnection();

			final String msg = UserMessages.getMessage("invalidApiUri", apiUrl);
			LogUtil.error(getLogContext(), msg, e);
			throw new AnaplanConnectionException(msg, e);
		}
		// fetch all stored credentials and properties
		final String username = connectionConfig.getStringProperty(USERNAME_FIELD);
		final String password = connectionConfig.getStringProperty(PASSWORD_FIELD);
		final String certLocation = connectionConfig.getStringProperty(CERT_PATH);
		final String proxyHost = connectionConfig.getStringProperty(URL_PROXY);
		final String proxyUser = connectionConfig.getStringProperty(URL_PROXY_USER);
		final String proxyPass = connectionConfig.getStringProperty(URL_PROXY_PASS);

		Credentials creds = null;
		try {
			// read in the certificate if provided, or fallback to basic
			// authentication.
			if (isCertificate)
				creds = new Credentials(readCertificate(certLocation));
			else
				creds = new Credentials(username, password, null, null);
			// Set the credentials on the service.
			service.setServiceCredentials(creds);
			// Set proxy credentials if provided.
			if (proxyHost != null && !proxyHost.isEmpty()) {
				service.setProxyLocation(new URI(proxyHost));
				if (proxyUser != null && !proxyUser.isEmpty()) {
					service.setProxyCredentials(new Credentials(proxyUser,
							proxyPass, null, null));
					LogUtil.debug(getLogContext(), "Proxy server configured");
				}
			}
		} catch (AnaplanAPIException e) {
			closeConnection();
			final String msg = UserMessages.getMessage("apiConnectFail",
					e.getMessage());
			LogUtil.error(getLogContext(), msg, e);
			throw new AnaplanConnectionException(msg, e);
		} catch (URISyntaxException e) {
			closeConnection();
			final String msg = UserMessages.getMessage("apiConnectFail",
					e.getMessage());
			LogUtil.error(getLogContext(), msg, e);
			throw new AnaplanConnectionException(msg, e);
		}

		LogUtil.debug(getLogContext(),
				"Anaplan service connection information cached");

		// validate username/password credentials by pulling workspaces for user
		List<Workspace> availableWorkspaces = null;
		try {
			availableWorkspaces = service.getWorkspaces();
		} catch (AnaplanAPIException e) {
			closeConnection();

			LogUtil.error(getLogContext(), e.getMessage(), e);

			if (e.getMessage() == null
					|| !e.getMessage().toLowerCase().contains("credentials")) {
				String exceptionDetails = e.getMessage();
				if (e.getCause() != null && e.getCause().getMessage() != null) {
					exceptionDetails = exceptionDetails + " ("
							+ e.getCause().getMessage() + ")";
				}
				throw new AnaplanConnectionException(exceptionDetails, e);
			}
			// else handle credentials issues below
		}

		if (availableWorkspaces == null || availableWorkspaces.isEmpty()) {
			final String msg = UserMessages.getMessage("accessFail");
			LogUtil.error(getLogContext(), msg + " (availableWorkspaces="
					+ availableWorkspaces + ")");
			throw new AnaplanConnectionException(msg);
		}

		LogUtil.debug(getLogContext(),
				"Anaplan service connection validated successfully");

		openConnection = service;

		return service;
	}

	/**
	 * Note that model's associated service object is left open for model
	 * access. Service should be closed by caller when access complete using
	 * {@link #closeConnection()}.
	 *
	 * If any open connection already exists it will be closed.
	 *
	 * @return null if workspace or model is not valid, else anaplan model
	 *         object
	 * @throws AnaplanConnectionException
	 *             With user-friendly message if the model cannot be opened.
	 */
	public Service openConnection() throws AnaplanConnectionException {
		LogUtil.status(getLogContext(), "Establishing connection....");
		if (openConnection == null) {
			LogUtil.status(getLogContext(), "No new connection found, "
					+ "establishing new connection!");
			return cacheService();
		} else {
			LogUtil.status(getLogContext(), "Connection exists, returning "
					+ "cached connection!");
			return this.openConnection;
		}
	}

	/**
	 * Getter for retrieving the open-connection, which is the service object
	 * to use for querying workspace and model details.
	 * @return
	 */
	public Service getConnection() {
		return this.openConnection;
	}

	/**
	 * Closes the open-connection if one exists.
	 */
	public void closeConnection() {
		if (openConnection != null) {
			openConnection.close();
		}
		openConnection = null;
		LogUtil.status(getLogContext(), "Connection closed.");
	}

	/**
	 * Get a log prefix of the form: [api url] [username].
	 * @return
	 */
	public String getLogContext() {
		final String apiUrl = connectionConfig.getStringProperty(URL_FIELD);
		final String username = connectionConfig
				.getStringProperty(USERNAME_FIELD);

		return wrap(apiUrl) + " " + wrap(username);
	}

	private String wrap(String s) {
		return "[" + s + "]";
	}
}
