package org.mule.modules.anaplan.connector.utils;

import java.io.IOException;

import org.mule.modules.anaplan.connector.AnaplanConnection;
import org.mule.modules.anaplan.connector.AnaplanResponse;
import org.mule.modules.anaplan.connector.exceptions.AnaplanExportOperationException;

import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.Model;
import com.anaplan.client.Service;
import com.anaplan.client.Workspace;


/**
 * Creates an export-task and executes it to data-dump Model contents and return
 * a <code>AnaplanResponse</code> object.
 *
 * @author spondonsaha
 */
public class AnaplanExportOperation {

	private final AnaplanConnection apiConn;
	private final Service service;
	private Workspace workspace = null;
	private Model model = null;

	/**
	 * Constructor
	 * @param apiConn
	 */
	public AnaplanExportOperation(AnaplanConnection apiConn) {
		this.apiConn = apiConn;
		this.service = apiConn.getConnection();
	}

	/**
	 * Fetches the workspace using the provided workspace ID.
	 * @param workspaceId
	 * @return Workspace keyed by provided "workspaceId".
	 * @throws AnaplanExportOperationException
	 */
	private Workspace getWorkspace(String workspaceId)
			throws AnaplanExportOperationException {

		try {
			workspace = service.getWorkspace(workspaceId);
			if (workspace == null) {
				throw new AnaplanExportOperationException("Could not fetch "
						+ "workspace with provided Workspace ID: "
						+ workspaceId);
			} else {
				LogUtil.status(apiConn.getLogContext(),
						"Workspace ID is valid: " + workspaceId);
			}
		} catch (AnaplanAPIException e) {
			throw new AnaplanExportOperationException("Error when fetching "
					+ "workspace for Workspace ID: " + workspaceId);
		}

		return workspace;
	}

	/**
	 * Fetches the model for the provided workspace and model IDs.
	 * @param workspaceId
	 * @param modelId
	 * @return Model keyed by the model and workspace IDs.
	 * @throws AnaplanExportOperationException
	 */
	private Model getModel(String workspaceId, String modelId)
			throws AnaplanExportOperationException {

		// get the workspace
		getWorkspace(workspaceId);

		try {
			model = workspace.getModel(modelId);
			if (model == null) {
				throw new AnaplanExportOperationException("Could not fetch "
						+ "model with provided model ID: "
						+ modelId);
			} else {
				LogUtil.status(apiConn.getLogContext(),
						"Model ID is valid: " + modelId);
			}
		} catch (AnaplanAPIException e) {
			throw new AnaplanExportOperationException("Error when fetching "
					+ "model for Workspace ID: " + workspaceId + ", Model ID"
					+ modelId);
		}

		return model;
	}

	/**
	 * Simple validation that tries to fetch the workspace and model using
	 * provided IDs. If any of the operation fails, then an exception is thrown.
	 * @param workspaceId
	 * @param modelId
	 * @param exportId
	 * @throws AnaplanExportOperationException
	 */
	private void validateExportDetails(String workspaceId, String modelId,
			String exportId) throws AnaplanExportOperationException {

		// validate workspace and model
		try {
			getModel(workspaceId, modelId);
		} catch (AnaplanExportOperationException e) {
			throw new AnaplanExportOperationException("Validation of Export "
					+ "details failed!!\n" + e.getMessage());
		}
		// validate export ID
		// TODO: Fetch JSON response for list of export-IDs, then validate
	}

	/**
	 * Performs the export operation against the model with the provided
	 * model-ID.
	 *
	 * @param modelId
	 * @return
	 * @throws AnaplanExportOperationException
	 */
	public String runExport(String workspaceId, String modelId, String exportId)
			throws AnaplanExportOperationException {
		final String logContext = apiConn.getLogContext();
		final String exportLogContext = logContext + " [" + exportId + "]";
		String response = null;

		LogUtil.status(logContext, "<< Starting export >>");
		LogUtil.status(logContext, "Workspace-ID: " + workspaceId);
		LogUtil.status(logContext, "Model-ID: " + modelId);
		LogUtil.status(logContext, "Export-ID: " + exportId);

		// validate that workspace, model and export-ID are valid.
		validateExportDetails(workspaceId, modelId, exportId);

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
			throw new AnaplanExportOperationException(e.getMessage(), e);
		} catch (AnaplanAPIException e) {
			throw new AnaplanExportOperationException(e.getMessage(), e);
		} finally {
			apiConn.closeConnection();
		}

		LogUtil.debug(exportLogContext, "export operation " + exportId
				+ " completed");
		return response;
	}
}
