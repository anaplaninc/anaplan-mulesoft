/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package com.anaplan.connector;

import org.mule.api.ConnectionException;
import org.mule.api.ConnectionExceptionCode;
import org.mule.api.annotations.Connect;
import org.mule.api.annotations.ConnectionIdentifier;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Disconnect;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.ValidateConnection;
import org.mule.api.annotations.display.FriendlyName;
import org.mule.api.annotations.display.Password;
import org.mule.api.annotations.param.ConnectionKey;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.api.annotations.param.Payload;

import com.anaplan.client.Service;
import com.anaplan.connector.exceptions.AnaplanConnectionException;
import com.anaplan.connector.exceptions.AnaplanOperationException;
import com.anaplan.connector.utils.AnaplanExecuteAction;
import com.anaplan.connector.utils.AnaplanExportOperation;
import com.anaplan.connector.utils.AnaplanImportOperation;
import com.anaplan.connector.utils.LogUtil;


/**
 * Anaplan Connector built using Anypoint Studio to export, upsert, delete of
 * data within models. It also supports running generic actions for performing
 * M2M import operations.
 *
 * @author MuleSoft, Inc.
 * @author Spondon Saha.
 */
@Connector(name = "anaplan", schemaVersion = "1.0", friendlyName = "Anaplan")
public class AnaplanConnector {

	private AnaplanConnection apiConn;
	private static AnaplanExportOperation exporter;
	private static AnaplanImportOperation importer;
	private static AnaplanExecuteAction runner;


	/**
	 * Reads in CSV data that represents an Anaplan model, delimited by the
	 * provided delimiter, parses it, then loads it into an Anaplan model.
	 *
	 * @param data
	 * @param anaplanWorkspaceId
	 * @param anaplanModelId
	 * @param anaplanImportId
	 * @param delimiter
	 * @throws AnaplanConnectionException
	 * @throws AnaplanOperationException
	 */
	@Processor(friendlyName = "Import")
	public void importToModel(
			@Payload String data,
			@FriendlyName("Workspace name or ID") String workspaceId,
		    @FriendlyName("Model name or ID") String modelId,
		    @FriendlyName("Import name or ID") String importId,
		    @Default("\t") String delimiter)
		    		throws AnaplanConnectionException,
		    			   AnaplanOperationException {
		// validate API connection
		validateConnection();

		// start the import
		importer = new AnaplanImportOperation(apiConn);
		importer.runImport(data, workspaceId, modelId, importId, delimiter);
	}

	/**
	 * Run an export of an Anaplan Model specified by workspace-ID, model-ID and
	 * the export-ID. At the end of each export, the connection is dropped,
	 * hence a check needs to be made to verify if the current connection
	 * exists. If not, re-establish it by calling .openConnection().
	 *
	 * @return CSV string.
	 * @throws AnaplanConnectionException
	 */
	@Processor(friendlyName = "Export")
	public String exportFromModel(
			@FriendlyName("Workspace name or ID") String workspaceId,
			@FriendlyName("Model name or ID") String modelId,
			@FriendlyName("Import name or ID") String exportId)
					throws AnaplanConnectionException,
						   AnaplanOperationException {
		// validate API connection
		validateConnection();

		// start the export
		exporter = new AnaplanExportOperation(apiConn);
		return exporter.runExport(workspaceId, modelId, exportId);
	}

	/**
	 * Deletes data from a model by executing the respective delete action.
	 *
	 * @throws AnaplanConnectionException
	 * @throws AnaplanOperationException
	 */
	@Processor(friendlyName = "Execute Action")
	public void executeAction(
			@FriendlyName("Workspace name or ID") String workspaceId,
			@FriendlyName("Model name or ID") String modelId,
			@FriendlyName("Import name or ID") String actionId)
					throws AnaplanConnectionException,
						   AnaplanOperationException {
		// validate the API connection
		validateConnection();

		// start the delete process
		runner = new AnaplanExecuteAction(apiConn);
		runner.runExecute(workspaceId, modelId, actionId);
	}

	/**
	 * Checks if the API connection has been established. If no connection
	 * object was found, then throws {@link AnaplanConnectionException}, else
	 * opens the connection and registers the service object.
	 *
	 * @throws AnaplanConnectionException
	 */
	private void validateConnection() throws AnaplanConnectionException {
		// validate API connection
		if (isConnected()) {
			if (apiConn.getConnection() == null) {
				apiConn.openConnection();
			} else {
				LogUtil.status(apiConn.getLogContext(),
						"Connection to API exists. Proceeding with export...");
			}
		} else {
			throw new AnaplanConnectionException(
					"No connection object: call connect()");
		}
	}

	/**
	 * Connect to the Anaplan API.
	 *
	 * @param username
	 * @param password
	 * @throws ConnectionException
	 */
	// TODO: Figure out what to do with ConnectionKey, as we need to either use
	// basic authentication, or certificate authentication. But the prompt
	// right now uses both. Need to figure out a way on the Mulesoft UI to allow
	// users to select an authentication type and accordingly provide the
	// relevant fields.
	@Connect
	public synchronized void connect(
			@ConnectionKey String username,
			@Password String password,
			@Optional @Default("") String certificatePath,
			@Default("https://api.anaplan.com/") String url,
			@Optional @Default("") String proxyHost,
			@Optional @Default("") String proxyUser,
			@Optional @Password @Default("") String proxyPass)
					throws org.mule.api.ConnectionException {

		LogUtil.status(getClass().toString(), "Initiating connection...");
		Service service = null;

		if (apiConn == null) {
			// create the connection object using credentials provided, or the
			// provided certificate.
			apiConn = new AnaplanConnection(certificatePath == "", username,
					password, url, certificatePath, proxyHost, proxyUser,
					proxyPass);
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
						+ "object acquired after opening connection to Anaplan "
						+ "API!", null);
			} else {
				LogUtil.status(getClass().toString(),
						"Successfully connected to Anaplan API!");
			}
		}
	}

	/**
	 * Disconnect
	 */
	@Disconnect
	public void disconnect() {
		if (apiConn != null) {
			apiConn.closeConnection();
		} else {
			LogUtil.error(getClass().toString(), "No connection to disconnect!");
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
}
