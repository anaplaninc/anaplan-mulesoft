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
import com.anaplan.connector.AnaplanResponse;
import com.anaplan.connector.connection.AnaplanConnection;
import com.anaplan.connector.exceptions.AnaplanOperationException;

/**
 * Used to Delete data from an Anaplan model.
 * @author spondonsaha
 */
public class AnaplanExecuteAction extends BaseAnaplanOperation {

	/**
	 * Constructor.
	 * @param apiConn
	 */
	public AnaplanExecuteAction(AnaplanConnection apiConn) {
		super(apiConn);
	}

	/**
	 * Performs a Deletion of records by executing the delete-action
	 * specified by the deleteId.
	 *
	 * @param workspaceId
	 * @param modelId
	 * @param actionId
	 * @throws AnaplanOperationException
	 */
	public String runExecute(String workspaceId, String modelId, String actionId)
			throws AnaplanOperationException {
		final String logContext = apiConn.getLogContext();
		final String exportLogContext = logContext + " [" + actionId + "]";

		LogUtil.status(logContext, "<< Starting Execute-Action >>");
		LogUtil.status(logContext, "Workspace-ID: " + workspaceId);
		LogUtil.status(logContext, "Model-ID: " + modelId);
		LogUtil.status(logContext, "Action ID: " + actionId);

		// validate that workspace, model and export-ID are valid.
		validateInput(workspaceId, modelId);

		// run the export
		try {
			final AnaplanResponse anaplanResponse = executeAction(model,
					actionId, exportLogContext);
			LogUtil.status(logContext, "Action complete: Status: "
					+ anaplanResponse.getStatus() + ", Response message: "
					+ anaplanResponse.getResponseMessage());

		} catch (AnaplanAPIException e) {
			throw new AnaplanOperationException(e.getMessage(), e);
		} finally {
			apiConn.closeConnection();
		}

		String statusMsg = "[" + actionId + "] completed successfully!";
		LogUtil.status(exportLogContext, statusMsg);
		return statusMsg;
	}
}
