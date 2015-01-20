/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

/**
 * Basic Anaplan Connection class that helps establish an API connection using
 * the provided credentials from the connector.
 *
 * Author: Spondon Saha
 */

package org.mule.modules.anaplan.connector;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.mule.modules.anaplan.connector.exceptions.AnaplanConnectionException;
import org.mule.modules.anaplan.connector.exceptions.ConnectorPropertiesException;
import org.mule.modules.anaplan.connector.utils.LogUtil;
import org.mule.modules.anaplan.connector.utils.UserMessages;

import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.Credentials;
import com.anaplan.client.Service;
import com.anaplan.client.Workspace;

/**
 * Validation and communication with the Anaplan model.
 *
 * This component has no individual access to the user-facing boomi logs: errors
 * reported here should be handled by the enclosing operation with the supplied
 * context from {@link #getLogContext()}.
 */
public class AnaplanConnection {

	private static final String URL_FIELD = "url";
	private static final String PASSWORD_FIELD = "password";
	private static final String USERNAME_FIELD = "username";
	private static final String URL_PROXY = "proxyHost";
	private static final String URL_PROXY_USER = "proxyUser";
	private static final String URL_PROXY_PASS = "proxyPass";

	private final AnaplanConnectorProperties connectionConfig;

	// cached Anaplan objects when a valid open connection exists, else null
	private Service openConnection = null;

	// private Model model = null;

	public AnaplanConnection(String... credentials) {
		LogUtil.debug("NOTICE: ", credentials[0] + " @ " + credentials[2]);
		// LOGGER.info("{} : {} ", credentials[0], credentials[1]);
		connectionConfig = new AnaplanConnectorProperties();
		try {
			connectionConfig.setProperties(credentials, USERNAME_FIELD,
					PASSWORD_FIELD, URL_FIELD, URL_PROXY, URL_PROXY_USER,
					URL_PROXY_PASS);
		} catch (ConnectorPropertiesException e) {
			LogUtil.error(getLogContext(),
					"Could not set connector properties!"
							+ e.getStackTrace().toString());
		}
		LogUtil.status(getLogContext(), "Stored connection properties!");
	}

	public String getConnectionId() {
		return this.toString();
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
		// try {
		// connectionConfig.hasAllRequiredProperties(true);
		// } catch (IllegalStateException e) {
		// closeConnection();
		//
		// LogUtil.error(getLogContext(), e.getMessage(), e);
		// throw new AnaplanConnectionException(e.getMessage(), e);
		// }

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

		final String username = connectionConfig
				.getStringProperty(USERNAME_FIELD);
		final String password = connectionConfig
				.getStringProperty(PASSWORD_FIELD);
		final String proxyHost = connectionConfig.getStringProperty(URL_PROXY);
		final String proxyUser = connectionConfig
				.getStringProperty(URL_PROXY_USER);
		final String proxyPass = connectionConfig
				.getStringProperty(URL_PROXY_PASS);

		try {
			service.setServiceCredentials(new Credentials(username, password,
					null, null));
			// TODO write simple unit test after extracting method
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

			// I think credentials only fail here if using certificate auth:
			// invalid username/password are ok until workspace retrieval
			final String msg = UserMessages.getMessage("apiConnectFail",
					e.getMessage());
			LogUtil.error(getLogContext(), msg, e);
			throw new AnaplanConnectionException(msg, e);
		} catch (URISyntaxException e) {
			closeConnection();

			// I think credentials only fail here if using certificate auth:
			// invalid username/password are ok until workspace retrieval
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
				// bug INTBOOMI-72: cases covered include incorrect api url, but
				// this detail is only found in the chained exception message.
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

	// public String getOperationType() {
	// return getContext().getOperationType().toString();
	// }

	/**
	 * Get a log prefix of the form: [api url] [username].
	 *
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
