package com.anaplan.connector.utils;

import org.mule.modules.anaplan.connector.AnaplanConnection;
import org.mule.modules.anaplan.connector.AnaplanConnectorProperties;
import org.mule.modules.anaplan.connector.AnaplanResponse;

import com.anaplan.connector.exceptions.ConnectorPropertiesException;

/**
 * Creates an export-task and executes it to data-dump Model contents
 * and return a <code>AnaplanResponse</code> object.
 * 
 * @author spondonsaha
 */
public class AnaplanExportOperation {
	
	private static final String EXPORT_ID_FIELD = "exportId";
	private final AnaplanConnectorProperties operationProperties;
	private final AnaplanConnection apiConn;
	
	/**
	 * Constructor
	 * @param apiConn
	 */
	public AnaplanExportOperation(AnaplanConnection apiConn) {
		this.apiConn = apiConn;
		this.operationProperties = new AnaplanConnectorProperties();
	}
	
	/**
	 * Performs the export operation against the model with the provided
	 * model-ID.
	 * @param modelId
	 * @return 
	 */
	public AnaplanResponse runExport(String modelId) {
		final String logContext = apiConn.getLogContext();
		LogUtil.debug(logContext, "<< Starting export >>");
		LogUtil.trace(logContext, "OperationProperties: " + operationProperties);
		LogUtil.trace(logContext, "Model-ID: " + modelId);
		
		String[] modelIdArr = modelId.split("");
		try {
			this.operationProperties.setProperties(modelIdArr, EXPORT_ID_FIELD);
		} catch (ConnectorPropertiesException e) {
			LogUtil.error(apiConn.getLogContext(), e.getMessage());
		}
		
		final String exportId = operationProperties.getStringProperty(
				EXPORT_ID_FIELD);
		final String exportLogContext = logContext + " [" + exportId + "]";
		LogUtil.status(apiConn.getLogContext(), 
				"Starting query (Anaplan export " + exportId + ")");
		try {
			final AnaplanResponse anaplanResponse = AnaplanUtil.runExport(
					apiConn, exportId, exportLogContext);
			anaplanResponse.writeExportData(apiConn, response,
					exportId, logContext);

			operationLog.status("Query complete: Status: "
					+ anaplanResponse.getStatus() + ", Response message: "
					+ anaplanResponse.getResponseMessage());

		} catch (IOException | AnaplanAPIException e) {
			AnaplanResponse.responseEpicFail(response, validInput, apiConn,
					e, null);
		} finally {
			apiConn.closeConnection();
		}

		LogUtil.debug(exportLogContext, "export operation " + exportId
				+ " completed");
	}
	
}
