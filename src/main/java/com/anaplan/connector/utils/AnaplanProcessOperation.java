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
import com.anaplan.client.Process;
import com.anaplan.client.Task;
import com.anaplan.client.TaskStatus;
import com.anaplan.connector.MulesoftAnaplanResponse;
import com.anaplan.connector.connection.AnaplanConnection;
import com.anaplan.connector.exceptions.AnaplanOperationException;


/**
 * Used for running Anaplan processes.
 * @author spondonsaha
 */
public class AnaplanProcessOperation extends BaseAnaplanOperation {

	/**
	 * Constructor.
	 *
	 * @param apiConn Anaplan API connection object.
	 */
	public AnaplanProcessOperation(AnaplanConnection apiConn) {
		super(apiConn);
	}

	/**
	 * Runs/Executes an Anaplan Process using the provided model, process-ID and
	 * log-context.
	 *
	 * @param model Anaplan Model object.
	 * @param processId Anaplan Process ID.
	 * @param logContext Log context for Process operations.
	 * @return Anaplan response object with execution details.
	 * @throws AnaplanAPIException thrown if fetching the process or creating the task
	 *                             to run the process is met with failure.
	 */
	private MulesoftAnaplanResponse runProcessTask(Model model, String processId,
			String logContext) throws AnaplanAPIException {

		final Process process = model.getProcess(processId);

		if (process == null) {
			final String msg = UserMessages.getMessage("invalidProcess",
					processId);
			return MulesoftAnaplanResponse.runProcessFailure(msg, null, logContext);
		}

		final Task task = process.createTask();
		final TaskStatus status = AnaplanUtil.runServerTask(task, logContext);

		if (status.getTaskState() == TaskStatus.State.COMPLETE &&
			status.getResult().isSuccessful()) {

			LogUtil.status(logContext, "Process ran successfully!");

			// Collect all the status details of running the action
			setRunStatusDetails(collectTaskLogs(status));

			return MulesoftAnaplanResponse.runProcessSuccess(
					status.getTaskState().name(),
					logContext);
		} else {
			return MulesoftAnaplanResponse.runProcessFailure("Run Process failed!",
					null, logContext);
		}
	}

	/**
	 * Triggers execution of an Anaplan process, collects the failure dump if
	 * any and returns it.
	 *
	 * @param workspaceId Anaplan workspace ID
	 * @param modelId Anaplan Model ID
	 * @param processId Anaplan Process ID
	 * @return Status message string for running process.
	 * @throws AnaplanOperationException thrown if runProcessTask() met with failure.
	 */
	public String runProcess(String workspaceId, String modelId,
			String processId) throws AnaplanOperationException {
		final String processLogContext = apiConn.getLogContext() + "["
			+ processId + "]";

		LogUtil.status(apiConn.getLogContext(), "<< Starting Process >>");
		LogUtil.status(apiConn.getLogContext(), "Workspace-ID: " + workspaceId);
		LogUtil.status(apiConn.getLogContext(), "Model-ID: " + modelId);
		LogUtil.status(apiConn.getLogContext(), "Process-ID: " + processId);

		// validate workspace-ID and model-ID are valid, else throw exception.
		validateInput(workspaceId, modelId);

		try {
			LogUtil.status(processLogContext, "Starting process: "+ processId);
			final MulesoftAnaplanResponse anaplanResponse = runProcessTask(model,
					processId, processLogContext);
			LogUtil.status(processLogContext, "Process ran successfully:"
					+ anaplanResponse.getStatus() + ", Response message: "
					+ anaplanResponse.getResponseMessage());
		} catch (AnaplanAPIException e) {
			throw new AnaplanOperationException(e.getMessage(), e);
		} finally {
			apiConn.closeConnection();
		}

		String statusMsg = "[" + processId + "] completed successfully!";
		LogUtil.status(processLogContext, statusMsg);
		return statusMsg + "\n\n" + getRunStatusDetails();
	}
}
