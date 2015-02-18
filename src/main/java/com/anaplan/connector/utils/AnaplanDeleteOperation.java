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


import com.anaplan.client.Action;
import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.Model;
import com.anaplan.client.Task;
import com.anaplan.client.TaskResult;
import com.anaplan.client.TaskResultDetail;
import com.anaplan.client.TaskStatus;
import com.anaplan.connector.AnaplanResponse;
import com.anaplan.connector.connection.AnaplanConnection;
import com.anaplan.connector.exceptions.AnaplanOperationException;


/**
 * Used to Delete data from an Anaplan model.
 * @author spondonsaha
 */
public class AnaplanDeleteOperation extends BaseAnaplanOperation {

	/**
	 * Constructor.
	 * @param apiConn
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
	 * @param model
	 * @param actionId
	 * @param logContext
	 * @return
	 * @throws AnaplanAPIException
	 */
	private static AnaplanResponse runDeleteAction(Model model, String actionId,
			String logContext) throws AnaplanAPIException {

		final Action action = model.getAction(actionId);

		if (action == null) {
			final String msg = UserMessages.getMessage("invalidAction",
					actionId);
			return AnaplanResponse.executeActionFailure(msg, null, logContext);
		}

		final Task task = action.createTask();
		final TaskStatus status = AnaplanUtil.runServerTask(task,logContext);

		if (status.getTaskState() == TaskStatus.State.COMPLETE &&
		    status.getResult().isSuccessful()) {
			LogUtil.status(logContext, "Action executed successfully.");

			// Collect all the status details for running the action.
			final TaskResult taskResult = status.getResult();
			final StringBuilder taskDetails = new StringBuilder();
			if (taskResult.getDetails() != null) {
				for (TaskResultDetail detail : taskResult.getDetails()) {
					taskDetails.append("\n" + detail.getLocalizedMessageText());
				}
				runStatusDetails = taskDetails.toString();
			}

			return AnaplanResponse.executeActionSuccess(
					status.getTaskState().name(),
					logContext);
		} else {
			return AnaplanResponse.executeActionFailure("Execute Action Failed",
					null, logContext);
		}
	}

	/**
	 * Performs a Deletion of records by executing the delete-action
	 * specified by the deleteId.
	 *
	 * @param workspaceId
	 * @param modelId
	 * @param deleteActionId
	 * @throws AnaplanOperationException
	 */
	public String runDeleteAction(String workspaceId, String modelId,
			String deleteActionId) throws AnaplanOperationException {
		final String logContext = apiConn.getLogContext();
		final String exportLogContext = logContext + " [" + deleteActionId + "]";

		LogUtil.status(logContext, "<< Starting Delete-Action >>");
		LogUtil.status(logContext, "Workspace-ID: " + workspaceId);
		LogUtil.status(logContext, "Model-ID: " + modelId);
		LogUtil.status(logContext, "Delete Action ID: " + deleteActionId);

		// validate that workspace, model and export-ID are valid.
		validateInput(workspaceId, modelId);

		// run the export
		try {
			final AnaplanResponse anaplanResponse = runDeleteAction(model,
					deleteActionId, exportLogContext);
			LogUtil.status(logContext, "Action complete: Status: "
					+ anaplanResponse.getStatus() + ", Response message: "
					+ anaplanResponse.getResponseMessage());

		} catch (AnaplanAPIException e) {
			throw new AnaplanOperationException(e.getMessage(), e);
		} finally {
			apiConn.closeConnection();
		}

		String statusMsg = "[" + deleteActionId + "] completed successfully!";
		LogUtil.status(exportLogContext, statusMsg);
		return statusMsg + "\n\n" + getRunStatusDetails();
	}
}
