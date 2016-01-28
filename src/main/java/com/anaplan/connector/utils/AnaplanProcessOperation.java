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
import com.anaplan.client.Process;
import com.anaplan.connector.MulesoftAnaplanResponse;
import com.anaplan.connector.connection.AnaplanConnection;
import com.anaplan.connector.exceptions.AnaplanOperationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Used for running Anaplan processes.
 * @author spondonsaha
 */
public class AnaplanProcessOperation extends BaseAnaplanOperation {

	private static Logger logger = LogManager.getLogger(
            AnaplanProcessOperation.class.getName());

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
	 * @return Anaplan response object with execution details.
	 * @throws AnaplanAPIException thrown if fetching the process or creating the task
	 *                             to run the process is met with failure.
	 */
	private MulesoftAnaplanResponse runProcessTask(Model model, String processId)
            throws AnaplanAPIException {

		final Process process = model.getProcess(processId);

		if (process == null) {
			final String msg = UserMessages.getMessage("invalidProcess",
					processId);
			return MulesoftAnaplanResponse.runProcessFailure(msg, null);
		}

		final Task task = process.createTask();
		final TaskStatus status = AnaplanUtil.runServerTask(task);

		if (status.getTaskState() == TaskStatus.State.COMPLETE &&
			status.getResult().isSuccessful()) {

			logger.info("Process ran successfully!");

			// Collect all the status details of running the action
			setRunStatusDetails(collectTaskLogs(status));

			return MulesoftAnaplanResponse.runProcessSuccess(
					status.getTaskState().name());
		} else {
			return MulesoftAnaplanResponse.runProcessFailure("Run Process failed!",
					null);
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

		logger.info("<< Starting Process >>");
		logger.info("Workspace-ID: {}", workspaceId);
		logger.info("Model-ID: {}", modelId);
		logger.info("Process-ID: {}", processId);

		// validate workspace-ID and model-ID are valid, else throw exception.
		validateInput(workspaceId, modelId);

		try {
			logger.info("Starting process: {}", processId);
			final MulesoftAnaplanResponse anaplanResponse = runProcessTask(model,
					processId);
			logger.info("Process ran successfully: {}, Response message: {}",
					anaplanResponse.getStatus(),
					anaplanResponse.getResponseMessage());
		} catch (AnaplanAPIException e) {
			throw new AnaplanOperationException(e.getMessage(), e);
		} finally {
			apiConn.closeConnection();
		}

		String statusMsg = "[" + processId + "] completed successfully!";
		logger.info(statusMsg);
		return statusMsg + "\n\n" + getRunStatusDetails();
	}
}
