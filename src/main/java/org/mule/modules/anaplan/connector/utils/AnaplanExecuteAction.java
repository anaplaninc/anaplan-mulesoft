/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */


package org.mule.modules.anaplan.connector.utils;

import org.mule.modules.anaplan.connector.AnaplanConnection;
import org.mule.modules.anaplan.connector.AnaplanResponse;
import org.mule.modules.anaplan.connector.exceptions.AnaplanOperationException;

import com.anaplan.client.AnaplanAPIException;

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
	public void runDelete(String workspaceId, String modelId, String actionId)
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
			LogUtil.status(logContext, "Deletion complete: Status: "
					+ anaplanResponse.getStatus() + ", Response message: "
					+ anaplanResponse.getResponseMessage());

		} catch (AnaplanAPIException e) {
			throw new AnaplanOperationException(e.getMessage(), e);
		} finally {
			apiConn.closeConnection();
		}

		LogUtil.status(exportLogContext, "Execute action " + actionId
				+ " completed");
	}
}
