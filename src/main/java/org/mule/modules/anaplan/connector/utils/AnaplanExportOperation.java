/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package org.mule.modules.anaplan.connector.utils;

import java.io.IOException;

import org.mule.modules.anaplan.connector.AnaplanConnection;
import org.mule.modules.anaplan.connector.AnaplanResponse;
import org.mule.modules.anaplan.connector.exceptions.AnaplanOperationException;

import com.anaplan.client.AnaplanAPIException;


/**
 * Creates an export-task and executes it to data-dump Model contents and return
 * a <code>AnaplanResponse</code> object.
 *
 * @author spondonsaha
 */
public class AnaplanExportOperation extends BaseAnaplanOperation {

	/**
	 * Constructor
	 * @param apiConn
	 */
	public AnaplanExportOperation(AnaplanConnection apiConn) {
		super(apiConn);
	}

	/**
	 * Exports a model as a CSV using the provided workspace-ID, model-ID and
	 * the export-ID.
	 * @param workspaceId
	 * @param modelId
	 * @param exportId
	 * @return
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
			final AnaplanResponse anaplanResponse = AnaplanUtil.doExport(
					model, exportId, exportLogContext);
			response = anaplanResponse.writeExportData(apiConn, exportId,
					logContext);
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

		LogUtil.status(exportLogContext, "export operation " + exportId
				+ " completed");
		return response;
	}
}
