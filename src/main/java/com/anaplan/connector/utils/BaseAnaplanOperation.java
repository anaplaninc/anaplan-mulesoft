/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package com.anaplan.connector.utils;


import com.anaplan.client.Action;
import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.Model;
import com.anaplan.client.Service;
import com.anaplan.client.Task;
import com.anaplan.client.TaskStatus;
import com.anaplan.client.Workspace;
import com.anaplan.connector.AnaplanConnection;
import com.anaplan.connector.AnaplanResponse;
import com.anaplan.connector.exceptions.AnaplanOperationException;


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
	 *
	 * @param apiConn
	 */
	private void setApiConn(AnaplanConnection apiConn) {
		this.apiConn = apiConn;
		this.service = apiConn.getConnection();
	}

	/**
	 * Fetches the workspace using the provided workspace ID.
	 *
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
	 *
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
	 *
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
	protected static AnaplanResponse executeAction(Model model, String actionId,
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
			return AnaplanResponse.executeActionSuccess(
					status.getTaskState().name(),
					logContext);
		} else {
			return AnaplanResponse.executeActionFailure("Execute Action Failed",
					null, logContext);
		}
	}
}
