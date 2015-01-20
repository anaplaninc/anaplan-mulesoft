package org.mule.modules.anaplan.connector.utils;

import org.mule.modules.anaplan.connector.AnaplanConnection;
import org.mule.modules.anaplan.connector.exceptions.AnaplanOperationException;

import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.Model;
import com.anaplan.client.Service;
import com.anaplan.client.Workspace;


/**
 * Base class for Import, Export, Update and Delete operations for Anaplan
 * models. Provides validation logic for workspace and models.
 * @author spondonsaha
 */
public class BaseAnaplanOperation {

	protected AnaplanConnection apiConn;
	protected Service service;
	protected Workspace workspace = null;
	protected Model model = null;

	public BaseAnaplanOperation(AnaplanConnection apiConn) {
		setApiConn(apiConn);
	}

	/**
	 * Helper method to set internal reference of API connection and service
	 * parameters.
	 * @param apiConn
	 */
	private void setApiConn(AnaplanConnection apiConn) {
		this.apiConn = apiConn;
		this.service = apiConn.getConnection();
	}

	/**
	 * Fetches the workspace using the provided workspace ID.
	 * @param workspaceId
	 * @return Workspace keyed by provided "workspaceId".
	 * @throws BaseAnaplanOperationException
	 */
	public Workspace getWorkspace(String workspaceId)
			throws AnaplanOperationException {

		try {
			workspace = service.getWorkspace(workspaceId);
			if (workspace == null) {
				throw new AnaplanOperationException("Could not fetch "
						+ "workspace with provided Workspace ID: "
						+ workspaceId);
			}
		} catch (AnaplanAPIException e) {
			throw new AnaplanOperationException("Error when fetching "
					+ "workspace for Workspace ID: " + workspaceId);
		}

		return workspace;
	}

	/**
	 * Fetches the model for the provided workspace and model IDs.
	 * @param workspaceId
	 * @param modelId
	 * @return Model keyed by the model and workspace IDs.
	 * @throws BaseAnaplanOperationException
	 */
	public Model getModel(String workspaceId, String modelId)
			throws AnaplanOperationException {

		// get the workspace
		getWorkspace(workspaceId);

		try {
			model = workspace.getModel(modelId);
			if (model == null) {
				throw new AnaplanOperationException("Could not fetch "
						+ "model with provided model ID: " + modelId);
			}
		} catch (AnaplanAPIException e) {
			throw new AnaplanOperationException("Error when fetching "
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
	 * @throws BaseAnaplanOperationException
	 */
	public void validateInput(String workspaceId, String modelId)
			throws AnaplanOperationException {

		// validate workspace and model
		getModel(workspaceId, modelId);
		LogUtil.status(apiConn.getLogContext(),
				"Workspace ID is valid: " + workspaceId);
		LogUtil.status(apiConn.getLogContext(),
				"Model ID is valid: " + modelId);
		// validate export ID
		// TODO: Fetch JSON response for list of export-IDs, then validate
	}
}
