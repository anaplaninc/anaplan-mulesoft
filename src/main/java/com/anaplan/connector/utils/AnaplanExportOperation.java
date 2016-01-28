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
import com.anaplan.client.Export;
import com.anaplan.client.ExportMetadata;
import com.anaplan.client.Model;
import com.anaplan.client.ServerFile;
import com.anaplan.client.Task;
import com.anaplan.client.TaskStatus;
import com.anaplan.connector.MulesoftAnaplanResponse;
import com.anaplan.connector.connection.AnaplanConnection;
import com.anaplan.connector.exceptions.AnaplanOperationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


/**
 * Creates an export-task and executes it to data-dump Model contents and return
 * an <code>AnaplanResponse</code> object.
 *
 * @author spondonsaha
 */
public class AnaplanExportOperation extends BaseAnaplanOperation {

    private static Logger logger = LogManager.getLogger(
            AnaplanExportOperation.class.getName());

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
	 * @return <code>AnaplanResponse</code> object.
	 * @throws AnaplanAPIException Thrown when error creating export task, or
	 *                             running it, or when building the response
	 */
	private static MulesoftAnaplanResponse doExport(Model model, String exportId)
			throws AnaplanOperationException {

		Export exp;
		try {
			exp = model.getExport(exportId);
		} catch (AnaplanAPIException e) {
			throw new AnaplanOperationException("Error fetching Export action:", e);
		}
		if (exp == null) {
			throw new AnaplanOperationException(UserMessages.getMessage(
					"invalidExport", exportId));
		}

		Task task;
		TaskStatus status;
		try {
			task = exp.createTask();
			status = AnaplanUtil.runServerTask(task);
		} catch (AnaplanAPIException e) {
			throw new AnaplanOperationException("Error running Export action:", e);
		}

		ExportMetadata exportMetadata;
		try {
			exportMetadata = exp.getExportMetadata();
		} catch (AnaplanAPIException e) {
			throw new AnaplanOperationException("Error fetching Export-metadata!");
		}
		if (status.getTaskState() == TaskStatus.State.COMPLETE &&
			status.getResult().isSuccessful()) {
			logger.info("Export completed successfully!");

			ServerFile serverFile;
			try {
				serverFile = model.getServerFile(exp.getName());
				if (serverFile == null) {
					throw new AnaplanOperationException(UserMessages.getMessage(
							"exportRetrieveError", exp.getName()));
				}
			} catch (AnaplanAPIException e) {
				throw new AnaplanOperationException("Error fetching export " +
						"Server-File:", e);
			}
			// collect all server messages regarding the export, if any
			setRunStatusDetails(collectTaskLogs(status));
			logger.info(getRunStatusDetails());

			return MulesoftAnaplanResponse.exportSuccess(status.getTaskState().name(),
					serverFile, exportMetadata);
		} else {
			logger.error("Export failed !!!");
			return MulesoftAnaplanResponse.exportFailure(status.getTaskState().name(),
					exportMetadata, null);
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

		String exportData, response;

		logger.info("<< Starting export >>");
		logger.info("Workspace-ID: {}", workspaceId);
		logger.info("Model-ID: {}", modelId);
		logger.info("Export-ID: {}", exportId);

		// validate that workspace, model and export-ID are valid.
		validateInput(workspaceId, modelId);

		// run the export
		MulesoftAnaplanResponse anaplanResponse = null;
		try {
			anaplanResponse = doExport(model, exportId);
			response = createResponse(anaplanResponse);
			exportData = anaplanResponse.writeExportData(apiConn);
			logger.info("Query complete: Status: {}, Response message: {}",
					anaplanResponse.getStatus(),
					anaplanResponse.getResponseMessage());

		} catch (IOException | AnaplanAPIException e) {
			throw new AnaplanOperationException(e.getMessage(), e);
		} finally {
			apiConn.closeConnection();
		}

		logger.info("{}", response);
		return exportData;
	}
}
