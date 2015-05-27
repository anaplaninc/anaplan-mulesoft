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

import java.io.IOException;

import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.Export;
import com.anaplan.client.Model;
import com.anaplan.client.ServerFile;
import com.anaplan.client.Task;
import com.anaplan.client.TaskStatus;
import com.anaplan.connector.MulesoftAnaplanResponse;
import com.anaplan.connector.connection.AnaplanConnection;
import com.anaplan.connector.exceptions.AnaplanOperationException;


/**
 * Creates an export-task and executes it to data-dump Model contents and return
 * an <code>AnaplanResponse</code> object.
 *
 * @author spondonsaha
 */
public class AnaplanExportOperation extends BaseAnaplanOperation {

	/**
	 * Constructor
	 * @param apiConn Anaplan API connection
	 */
	public AnaplanExportOperation(AnaplanConnection apiConn) {
		super(apiConn);
	}

	/**
	 * Performs the Model export operation.
	 *
	 * @param model Anaplan Model object.
	 * @param exportId Anaplan Export ID
	 * @param logContext Export log context
	 * @return <code>AnaplanResponse</code> object.
	 * @throws AnaplanAPIException Thrown when error creating export task, or
	 *                             running it, or when building the response
	 */
	private static MulesoftAnaplanResponse doExport(Model model, String exportId,
			String logContext) throws AnaplanAPIException {

		final Export exp = model.getExport(exportId);
		if (exp == null) {
			final String msg = UserMessages.getMessage("invalidExport", exportId);
			return MulesoftAnaplanResponse.exportFailure(msg, null, null, logContext);
		}

		final Task task = exp.createTask();
		final TaskStatus status = AnaplanUtil.runServerTask(task, logContext);

		if (status.getTaskState() == TaskStatus.State.COMPLETE &&
			status.getResult().isSuccessful()) {
			LogUtil.status(logContext, "Export completed successfully!");
			final ServerFile serverFile = model.getServerFile(exp.getName());
			if (serverFile == null) {
				return MulesoftAnaplanResponse.exportFailure(
						UserMessages.getMessage("exportRetrieveError",
								exp.getName()), exp.getExportMetadata(), null,
						logContext);
			}
			// collect all server messages regarding the export, if any
			setRunStatusDetails(collectTaskLogs(status));
			LogUtil.status(logContext, getRunStatusDetails());

			return MulesoftAnaplanResponse.exportSuccess(status.getTaskState().name(),
					serverFile, exp.getExportMetadata(), logContext);
		} else {
			LogUtil.error(logContext, "Export failed !!!");
			return MulesoftAnaplanResponse.exportFailure(status.getTaskState().name(),
					exp.getExportMetadata(), null, logContext);
		}
	}

	/**
	 * Exports a model as a CSV using the provided workspace-ID, model-ID and
	 * the export-ID.
	 * @param workspaceId Anaplan Workspace ID
	 * @param modelId Anaplan Model ID
	 * @param exportId Anaplan Export action ID
	 * @return Export string message from running export action.
	 * @throws AnaplanOperationException
	 */
	public String runExport(String workspaceId, String modelId, String exportId)
			throws AnaplanOperationException {
		final String logContext = apiConn.getLogContext();
		final String exportLogContext = logContext + " [" + exportId + "]";
		String response = null;

		LogUtil.status(logContext, "<< Starting export >>");
		LogUtil.status(logContext, "Workspace-ID: " + workspaceId);
		LogUtil.status(logContext, "Model-ID: " + modelId);
		LogUtil.status(logContext, "Export-ID: " + exportId);

		// validate that workspace, model and export-ID are valid.
		validateInput(workspaceId, modelId);

		// run the export
		try {
			final MulesoftAnaplanResponse anaplanResponse = doExport(model,
					exportId, exportLogContext);
			response = anaplanResponse.writeExportData(apiConn);
			LogUtil.status(logContext, "Query complete: Status: "
					+ anaplanResponse.getStatus() + ", Response message: "
					+ anaplanResponse.getResponseMessage());

		} catch (IOException e) {
			throw new AnaplanOperationException(e.getMessage(), e);
		} catch (AnaplanAPIException e) {
			throw new AnaplanOperationException(e.getMessage(), e);
		} finally {
			apiConn.closeConnection();
		}

		LogUtil.status(exportLogContext, "[" + exportId + "] ran successfully!");
		return response;
	}
}
