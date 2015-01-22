
package org.mule.modules.anaplan.connector.utils;

import org.mule.modules.anaplan.connector.AnaplanConnection;
import org.mule.modules.anaplan.connector.AnaplanResponse;
import org.mule.modules.anaplan.connector.exceptions.AnaplanOperationException;

import com.anaplan.client.AnaplanAPIException;

/**
 * Used to Delete data from an Anaplan model.
 * @author spondonsaha
 */
public class AnaplanDeleteOperation extends BaseAnaplanOperation {

	/**
	 * Constructor.
	 * @param apiConn
	 */
	public AnaplanDeleteOperation(AnaplanConnection apiConn) {
		super(apiConn);
	}

	/**
	 * Performs a Deletion of records by executing the delete-action
	 * specified by the deleteId.
	 *
	 * @param workspaceId
	 * @param modelId
	 * @param deleteId
	 * @throws AnaplanOperationException
	 */
	public void runDelete(String workspaceId, String modelId, String deleteId)
			throws AnaplanOperationException {
		final String logContext = apiConn.getLogContext();
		final String exportLogContext = logContext + " [" + deleteId + "]";

		LogUtil.status(logContext, "<< Starting Delete >>");
		LogUtil.status(logContext, "Workspace-ID: " + workspaceId);
		LogUtil.status(logContext, "Model-ID: " + modelId);
		LogUtil.status(logContext, "Delete Action ID: " + deleteId);

		// validate that workspace, model and export-ID are valid.
		validateInput(workspaceId, modelId);

		// run the export
		try {
			final AnaplanResponse anaplanResponse = executeAction(model,
					deleteId, exportLogContext);
			LogUtil.status(logContext, "Deletion complete: Status: "
					+ anaplanResponse.getStatus() + ", Response message: "
					+ anaplanResponse.getResponseMessage());

		} catch (AnaplanAPIException e) {
			throw new AnaplanOperationException(e.getMessage(), e);
		} finally {
			apiConn.closeConnection();
		}

		LogUtil.status(exportLogContext, "Delete operation " + deleteId
				+ " completed");
	}

}
