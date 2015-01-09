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

import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.Credentials;
import com.anaplan.client.Model;
import com.anaplan.client.Service;
import com.anaplan.client.Workspace;
import com.anaplan.connector.exceptions.AnaplanConnectionException;
import com.anaplan.connector.exceptions.ConnectorPropertiesException;
import com.anaplan.connector.utils.LogUtil;
import com.anaplan.connector.utils.UserMessages;


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
	private static final String WORKSPACEID_FIELD = "workspaceId";
	private static final String MODELID_FIELD = "modelId";
	private static final String URL_PROXY = "proxyHost";
	private static final String URL_PROXY_USER = "proxyUser";
	private static final String URL_PROXY_PASS = "proxyPass";
	
	private final AnaplanConnectorProperties connectionConfig;

//	public static final Logger LOGGER = LogManager.getLogger(AnaplanConnection.class);
	
	// cached Anaplan objects when a valid open connection exists, else null
	private Service openConnection = null;
	private Model model = null;

	public AnaplanConnection(String... credentials) {
		LogUtil.debug("NOTICE: ", credentials[0] + " @ " + credentials[2]);
//		LOGGER.info("{} : {} ", credentials[0], credentials[1]);
		connectionConfig = new AnaplanConnectorProperties();
		try {
			connectionConfig.setProperties(credentials, USERNAME_FIELD, 
					PASSWORD_FIELD, URL_FIELD, WORKSPACEID_FIELD, MODELID_FIELD, 
					URL_PROXY, URL_PROXY_USER, URL_PROXY_PASS);
		} catch (ConnectorPropertiesException e) {
			LogUtil.error(getLogContext(), "Could not set connector properties!");
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
	 * @throws AnaplanConnectionException
	 *             If there was an error with the service or any or the required
	 *             properties.
	 */
	private void cacheService() throws AnaplanConnectionException {
		LogUtil.debug(getLogContext(), "trying Anaplan service connection.");
//		try {
//			connectionConfig.hasAllRequiredProperties(true);
//		} catch (IllegalStateException e) {
//			closeConnection();
//
//			LogUtil.error(getLogContext(), e.getMessage(), e);
//			throw new AnaplanConnectionException(e.getMessage(), e);
//		}

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

		final String username = connectionConfig.getStringProperty(USERNAME_FIELD);
		final String password = connectionConfig.getStringProperty(PASSWORD_FIELD);
		final String proxyHost = connectionConfig.getStringProperty(URL_PROXY);
		final String proxyUser = connectionConfig.getStringProperty(URL_PROXY_USER);
		final String proxyPass = connectionConfig.getStringProperty(URL_PROXY_PASS);

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

		// validate username/password credentials
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
	}

	/**
	 * Check whether workspace and model are valid, and cahe if they are.
	 * Requires a valid openConnection, i.e. cumulative with
	 * {@link #cacheService()}.
	 * 
	 * @throws AnaplanConnectionException
	 *             With user-friendly error message if validation failed
	 * @throws IllegalStateException
	 *             if no open connection available
	 */
	private void cacheWorkspaceAndModel() throws AnaplanConnectionException {
		LogUtil.debug(getLogContext(), "validating workspace and model");

		if (openConnection == null) {
			throw new IllegalStateException(
					"Can't retrieve workspace or model: Anaplan service has not been initialised");
		}

		final String workspaceId = connectionConfig.getStringProperty(
				WORKSPACEID_FIELD);
		final String modelId = connectionConfig.getStringProperty(
				MODELID_FIELD);
		try {
			final Workspace workspace = openConnection
					.getWorkspace(workspaceId);

			if (workspace == null) {
				throw new AnaplanConnectionException(UserMessages.getMessage(
						"invalidWorkspace", workspaceId));
			} else {
				LogUtil.debug(getLogContext(),
						"successfully retrieved workspace");
			}

			final Model model = workspace.getModel(modelId);
			if (model == null) {
				throw new AnaplanConnectionException(UserMessages.getMessage(
						"invalidModel", modelId));
			} else {
				LogUtil.debug(getLogContext(), "successfully retrieved model");
				this.model = model;
			}

		} catch (AnaplanAPIException e) {
			closeConnection();

			final String msg = "Error retrieving Anaplan model or workspace: "
					+ e.getMessage();
			LogUtil.error(getLogContext(), msg, e);

			throw new AnaplanConnectionException(msg, e);
		}

		LogUtil.debug(getLogContext(),
				"workspace and model validated successfully");
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
	public Model openConnection() throws AnaplanConnectionException {
		LogUtil.debug(getLogContext(), "openConnection()");
		if (openConnection != null) {
			LogUtil.warning(getLogContext(),
					"openConnection() request found existing open connection: closing.");
			closeConnection();
		}

		cacheService();
		cacheWorkspaceAndModel();

		return model;
	}

	/**
	 * Closes the open-connection if one exists.
	 */
	public void closeConnection() {
		if (openConnection != null) {
			openConnection.close();
		}

		openConnection = null;
		model = null;
	}

//	public String getOperationType() {
//		return getContext().getOperationType().toString();
//	}

	/**
	 * Get a log prefix of the form: [api url] [workspace] [model] [username].
	 * 
	 * @return
	 */
	public String getLogContext() {
		final String apiUrl = connectionConfig
				.getStringProperty(URL_FIELD);
		final String workspaceId = connectionConfig
				.getStringProperty(WORKSPACEID_FIELD);
		final String modelId = connectionConfig
				.getStringProperty(MODELID_FIELD);
		final String username = connectionConfig
				.getStringProperty(USERNAME_FIELD);

		return wrap(apiUrl) + " " + wrap(workspaceId) + " " + wrap(modelId)
				+ " " + wrap(username);
	}

	private String wrap(String s) {
		return "[" + s + "]";
	}
}
