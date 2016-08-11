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

package com.anaplan.connector.utils;


import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.Model;
import com.anaplan.client.Service;
import com.anaplan.client.TaskResult;
import com.anaplan.client.TaskResultDetail;
import com.anaplan.client.TaskStatus;
import com.anaplan.client.Workspace;
import com.anaplan.connector.MulesoftAnaplanResponse;
import com.anaplan.connector.connection.AnaplanConnection;
import com.anaplan.connector.exceptions.AnaplanOperationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;


/**
 * Base class for Import, Export, Update and Delete operations for Anaplan
 * models. Provides validation logic for workspace and models.
 *
 * @author spondonsaha
 */
public class BaseAnaplanOperation {

	private static Logger logger = LogManager.getLogger(
			BaseAnaplanOperation.class.getName());

	protected AnaplanConnection apiConn;
	protected Service service;
	protected Workspace workspace = null;
	protected Model model = null;
	// TODO: Needs a setter as well.
	private static String runStatusDetails = null;

	public BaseAnaplanOperation(AnaplanConnection apiConn) {
		setApiConn(apiConn);
	}

	/**
	 * Getter for the Run-status. The string for runStatusDetails is
	 * populated by executeAction().
	 *
	 * @return String containing the run-detail logs sent back from the server
	 * 		for running a particular action.
	 */
	public static String getRunStatusDetails() {
		return runStatusDetails;
	}

	/**
	 * Setter for runStatusDetails, usually server side logs of success or
	 * failure.
	 *
	 * @param statusMsgs String containing status message logs.
	 */
	public static void setRunStatusDetails(String statusMsgs) {
		runStatusDetails = statusMsgs;
	}

	/**
	 * Helper method to set internal reference of API connStrategy and service
	 * parameters.
	 *
	 * @param apiConn Anaplan API connection object.
	 */
	private void setApiConn(AnaplanConnection apiConn) {
		this.apiConn = apiConn;
		this.service = apiConn.getConnection();
	}

	/**
	 * Fetches the workspace using the provided workspace ID.
	 *
	 * @param workspaceId Anaplan Workspace ID.
	 * @return Workspace keyed by provided "workspaceId".
	 * @throws AnaplanOperationException Thrown when failure fetching workspace.
	 */
	public Workspace getWorkspace(String workspaceId)
			throws AnaplanOperationException {

		try {
			workspace = service.getWorkspace(workspaceId);
			if (workspace == null) {
				throw new AnaplanOperationException("Could not fetch "
						+ "workspace with provided Workspace ID: "
						+ workspaceId);
			}
		} catch (AnaplanAPIException e) {
			throw new AnaplanOperationException("Error when fetching "
					+ "workspace for Workspace ID: " + workspaceId);
		}

		return workspace;
	}

	/**
	 * Fetches the model for the provided workspace and model IDs.
	 *
	 * @param workspaceId Anaplan Workspace ID.
	 * @param modelId Anaplan Model ID.
	 * @return Model keyed by the model and workspace IDs.
	 * @throws AnaplanOperationException Thrown when failure fetching model.
	 */
	public Model getModel(String workspaceId, String modelId)
			throws AnaplanOperationException {

		// get the workspace
		getWorkspace(workspaceId);

		try {
			model = workspace.getModel(modelId);
			if (model == null) {
				throw new AnaplanOperationException("Could not fetch "
						+ "model with provided model ID: " + modelId);
			}
		} catch (AnaplanAPIException e) {
			throw new AnaplanOperationException("Error when fetching "
					+ "model for Workspace ID: " + workspaceId + ", Model ID"
					+ modelId);
		}
		return model;
	}

	/**
	 * Simple validation that tries to fetch the workspace and model using
	 * provided IDs. If any of the operation fails, then an exception is thrown.
	 *
	 * @param workspaceId Anaplan Workspace ID.
	 * @param modelId Anaplan Model ID.
	 * @throws AnaplanOperationException Thrown when error is encountered
	 * 		during fetching Workspace or Model.
	 */
	public void validateInput(String workspaceId, String modelId)
			throws AnaplanOperationException {

		// validate workspace and model
		getModel(workspaceId, modelId);
		logger.info("Workspace ID is valid: {}", workspaceId);
		logger.info("Model ID is valid: {}", modelId);
		// validate export ID
		// TODO: Fetch JSON response for list of export-IDs, then validate
	}

	/**
	 * Helper method to collect logs sent back from server, using provided
	 * TaskStatus object.
	 *
	 * @param status TaskStatus object containing task details
	 * @return Log string delimited by new-line, fetched from TaskStatus object.
	 */
	public String collectTaskLogs(TaskStatus status) {
		final TaskResult taskResult = status.getResult();
		final StringBuilder taskDetails = new StringBuilder();
		if (taskResult.getDetails() != null) {
			for (TaskResultDetail detail : taskResult.getDetails()) {
				taskDetails.append("\n" + detail.getLocalizedMessageText());
			}
			return taskDetails.toString();
		}
		return null;
	}

	/**
	 * Creating response based on operation status.
	 * TODO: Move this to Anaplan-Connect
	 * @param anaplanResponse Anaplan response containing API response details.
	 * @throws AnaplanOperationException
	 */
	protected String createResponse(MulesoftAnaplanResponse anaplanResponse)
			throws AnaplanOperationException {

		if (anaplanResponse == null) {
			throw new AnaplanOperationException("Null response found!");
		}

		// validate import to Anaplan
		OperationStatus os = anaplanResponse.getStatus();
		String responseMessage;
		switch (os) {
			case SUCCESS:
				responseMessage = MessageFormat.format("Import ran " +
						"successfully: {0}",
						anaplanResponse.getResponseMessage());
				break;
			case APPLICATION_ERROR:
				responseMessage = MessageFormat.format("Operation ran " +
						"successfully but with warnings!\nResponse Message:\n" +
						"{0}\nDump File contents:\n{1}",
						anaplanResponse.getResponseMessage(),
						anaplanResponse.getDumpFileContents());
				break;
			case FAILURE:
				throw new AnaplanOperationException(
						MessageFormat.format("Operation failed!\n{0}",
								anaplanResponse.getResponseMessage()));
			default:
				throw new AnaplanOperationException(
						"Could not determine run status of " + "Anaplan Import!");
		}
		return responseMessage;
	}
}
