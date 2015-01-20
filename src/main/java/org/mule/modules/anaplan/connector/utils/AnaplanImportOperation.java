package org.mule.modules.anaplan.connector.utils;

import java.io.IOException;

import org.mule.modules.anaplan.connector.AnaplanConnection;
import org.mule.modules.anaplan.connector.AnaplanResponse;
import org.mule.modules.anaplan.connector.exceptions.AnaplanOperationException;

import com.anaplan.client.AnaplanAPIException;
import com.google.gson.JsonSyntaxException;


/**
 * Used to import CSV data into Anaplan
 * @author spondonsaha
 */
public class AnaplanImportOperation extends BaseAnaplanOperation{

	/**
	 * Constructor
	 * @param conn
	 */
	public AnaplanImportOperation(AnaplanConnection apiConn) {
		super(apiConn);
	}

	/**
	 * Imports a model using the provided workspace-ID, model-ID and Import-ID.
	 * @param workspaceId
	 * @param modelId
	 * @param importId
	 * @throws AnaplanOperationException
	 */
	public void runImport(String data, String workspaceId, String modelId,
			String importId, String delimiter) throws AnaplanOperationException{

		final String importLogContext = apiConn.getLogContext() + " ["
				+ importId + "]";

		LogUtil.status(apiConn.getLogContext(), "<< Starting import >>");
		LogUtil.status(apiConn.getLogContext(), "Workspace-ID: " + workspaceId);
		LogUtil.status(apiConn.getLogContext(), "Model-ID: " + modelId);
		LogUtil.status(apiConn.getLogContext(), "Import-ID: " + importId);

		// validate workspace-ID and model-ID are valid, else throw exception
		validateInput(workspaceId, modelId);

		try {
			LogUtil.status(importLogContext, "Starting import: " + importId);

			final AnaplanResponse anaplanResponse = AnaplanUtil.runImportCsv(
					data, model, importId, delimiter, importLogContext);
			anaplanResponse.writeImportData(apiConn, importId, importLogContext);

			LogUtil.status(importLogContext, "Import complete: Status: "
					+ anaplanResponse.getStatus() + ", Response message: "
					+ anaplanResponse.getResponseMessage());

		} catch (IOException e) {
			AnaplanResponse.responseEpicFail(apiConn, e, null);
		} catch (JsonSyntaxException e) {
			AnaplanResponse.responseEpicFail(apiConn, e, null);
		} catch (AnaplanAPIException e) {
			AnaplanResponse.responseEpicFail(apiConn, e, null);
		} finally {
			apiConn.closeConnection();
		}

		LogUtil.debug(importLogContext, "import operation " + importId
				+ " completed");
	}
}