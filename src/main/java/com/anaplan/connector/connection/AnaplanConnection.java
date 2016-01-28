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
import com.anaplan.connector.utils.UserMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 */
public class AnaplanConnection {

	private static final Logger logger = LogManager.getLogger(
            AnaplanConnection.class.getName());

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
	 * @param isCertificate If set to true then just use certificate for
     *      authentication. If false, then we are using basic authentication and
     *      so just need username and password.
	 * @param credentials String[] of credential values, which includes the
     *      username, password, API url, certificate path and proxy URL and
     *      their credentials if using this connector behind a firewall.
	 */
	public AnaplanConnection(boolean isCertificate, String... credentials) {
		logger.debug("NOTICE: {} @ {}", credentials[0], credentials[2]);
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
			logger.error("Could not set connector properties!", e);
		}
		logger.info("Stored connection properties!");
	}

	/**
	 * Getter for the connection ID, which is the string representation of this
	 * object.
	 *
	 * @return Connection ID string.
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
	 * @param certificateLocation Location on file system of login certificate.
	 * @return X509Certificate object containing user certificate details.
	 * @throws AnaplanConnectionException Thrown if any sort of Certificate
	 *      parsing exception occurs.
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
				logger.info("Certificate VALID!");
				logger.debug(x509.toString());
			}
		} catch (CertificateException e) {
			throw new AnaplanConnectionException("Bad certificate", e);
        } catch (IOException e) {
        	throw new AnaplanConnectionException("Could not open certificate", e);
        } catch (Throwable e) {
			throw new AnaplanConnectionException("Unknown exception occurred", e);
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

	 * @return The service object that contains workspace/model details for the
	 *      authenticated user.
	 * @throws AnaplanConnectionException If there was an error with the service
     *      or any or the required properties.
	 */
	private Service cacheService() throws AnaplanConnectionException {
		logger.debug("Trying Anaplan service connection...");

		final String apiUrl = connectionConfig.getStringProperty(URL_FIELD);
		Service service;
        logger.debug("API Url: {}", apiUrl);
		try {
			service = new Service(new URI(apiUrl));
		} catch (URISyntaxException e) {
			closeConnection();
			throw new AnaplanConnectionException(
					UserMessages.getMessage("invalidApiUri", apiUrl), e);
		}
		// fetch all stored credentials and properties
		final String username = connectionConfig.getStringProperty(USERNAME_FIELD);
		final String password = connectionConfig.getStringProperty(PASSWORD_FIELD);
		final String certLocation = connectionConfig.getStringProperty(CERT_PATH);
		final String proxyHost = connectionConfig.getStringProperty(URL_PROXY);
		final String proxyUser = connectionConfig.getStringProperty(URL_PROXY_USER);
		final String proxyPass = connectionConfig.getStringProperty(URL_PROXY_PASS);

		Credentials creds;
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
					logger.debug("Proxy server configured");
				}
			}
		} catch (AnaplanAPIException | URISyntaxException e) {
			closeConnection();
			final String msg = UserMessages.getMessage("apiConnectFail",
					e.getMessage());
			logger.error(msg, e);
			throw new AnaplanConnectionException(msg, e);
		}

		logger.debug("Anaplan service connection information cached.");

		// validate username/password credentials by pulling workspaces for user
		List<Workspace> availableWorkspaces = null;
		try {
			availableWorkspaces = service.getWorkspaces();
		} catch (AnaplanAPIException e) {
			closeConnection();

			logger.error(e.getMessage(), e);

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
			logger.error("{} (availableWorkspaces={})", msg, availableWorkspaces);
			throw new AnaplanConnectionException(msg);
		}

		logger.debug("Anaplan service connection validated successfully");

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
		logger.info("Establishing connection....");
		if (openConnection == null) {
			logger.info("No new connection found, establishing new connection!");
			return cacheService();
		} else {
			logger.info("Connection exists, returning cached connection!");
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
		logger.info("Connection closed.");
	}
}
