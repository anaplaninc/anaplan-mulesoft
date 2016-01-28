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


import com.anaplan.client.*;
import com.anaplan.connector.MulesoftAnaplanResponse;
import com.anaplan.connector.connection.AnaplanConnection;
import com.anaplan.connector.exceptions.AnaplanOperationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Used to Delete data from an Anaplan model.
 * @author spondonsaha
 */
public class AnaplanDeleteOperation extends BaseAnaplanOperation {

	private static Logger logger = LogManager.getLogger(
            AnaplanDeleteOperation.class.getName());

	/**
	 * Constructor.
	 * @param apiConn Anaplan API connection object.
	 */
	public AnaplanDeleteOperation(AnaplanConnection apiConn) {
		super(apiConn);
	}

	/**
	 * Used to run delete or M2M operations, or any such action that does not
	 * rely on any input from the flow or outputs any data to the flow. This
	 * allows you to execute any inert operation within Anaplan's core
	 * infrastructure.
	 *
	 * @param model Anaplan Model object.
	 * @param actionId Anaplan Delete action ID.
	 * @return Response object containing details of executing delete.
	 * @throws AnaplanAPIException Thrown when errors at talkng to API.
	 */
	private static MulesoftAnaplanResponse runDeleteAction(Model model,
			String actionId) throws AnaplanAPIException {

		final Action action = model.getAction(actionId);

		if (action == null) {
			final String msg = UserMessages.getMessage("invalidAction",
					actionId);
			return MulesoftAnaplanResponse.executeActionFailure(msg, null);
		}

		final Task task = action.createTask();
		final TaskStatus status = AnaplanUtil.runServerTask(task);

		if (status.getTaskState() == TaskStatus.State.COMPLETE &&
		    status.getResult().isSuccessful()) {

			logger.info("Action executed successfully.");

			// Collect all the status details for running the action.
			setRunStatusDetails(collectTaskLogs(status));

			return MulesoftAnaplanResponse.executeActionSuccess(
					status.getTaskState().name());
		} else {
			return MulesoftAnaplanResponse.executeActionFailure("Execute Action Failed",
					null);
		}
	}

	/**
	 * Performs a Deletion of records by executing the delete-action
	 * specified by the deleteId.
	 *
	 * @param workspaceId Anaplan Workspace ID
	 * @param modelId Anaplan Model ID.
	 * @param deleteActionId Delete action ID.
	 * @throws AnaplanOperationException Rethrown as internal exception
	 * 		capturing AnaplanAPIException.
	 */
	public String runDeleteAction(String workspaceId, String modelId,
			String deleteActionId) throws AnaplanOperationException {

		logger.info("<< Starting Delete-Action >>");
		logger.info("Workspace-ID: {}", workspaceId);
		logger.info("Model-ID: {}", modelId);
		logger.info("Delete Action ID: {}", deleteActionId);

		// validate that workspace, model and export-ID are valid.
		validateInput(workspaceId, modelId);

		// run the export
		try {
			final MulesoftAnaplanResponse anaplanResponse = runDeleteAction(model,
					deleteActionId);
			logger.info("Action complete: Status: {}, Response message: {}",
					anaplanResponse.getStatus(),
					anaplanResponse.getResponseMessage());

		} catch (AnaplanAPIException e) {
			throw new AnaplanOperationException(e.getMessage(), e);
		} finally {
			apiConn.closeConnection();
		}

		String statusMsg = "[" + deleteActionId + "] completed successfully!";
		logger.info(statusMsg);
		return statusMsg + "\n\n" + getRunStatusDetails();
	}
}
