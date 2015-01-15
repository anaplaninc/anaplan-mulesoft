/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package org.mule.modules.anaplan.connector;

//import java.io.IOException;

import java.io.IOException;
import java.io.Serializable;

import org.mule.modules.anaplan.connector.utils.LogUtil;
import org.mule.modules.anaplan.connector.utils.OperationStatus;

import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.CellReader;
import com.anaplan.client.ExportMetadata;
import com.anaplan.client.ServerFile;

//import org.mule.modules.anaplan.utils.AnaplanUtil;

/**
 * Translates anaplan task results into boomi response handling.
 *
 * Provides constructors corresponding to the expected states of a completed
 * anaplan server task, utilities to convert these into boomi responses, and
 * shortcut methods to generate boomi failures (for use where e.g. failure
 * occurs before any anaplan task has been created).
 *
 * Payloads generated here form the data output from the anaplan connector. In
 * the case of an operation success or partial success, this is written out as
 * Current Data for a downstream connector to consume; in the case of complete
 * failure this payload is displayed in the UI if running Boomi in test mode, or
 * does ???? if running in production mode.
 */
public class AnaplanResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String responseMessage;
	private final ServerFile serverFile;
	private final ExportMetadata exportMetadata;
	private final OperationStatus status;
	private final String logContext;
	private final Throwable exception;

	public static AnaplanResponse exportFailure(String responseMessage,
			ExportMetadata exportMetadata, Throwable cause, String logContext) {
		return new AnaplanResponse(responseMessage, OperationStatus.FAILURE,
				null, exportMetadata, cause, logContext);
	}

	public static AnaplanResponse importFailure(String responseMessage,
			Throwable cause, String logContext) {
		return new AnaplanResponse(responseMessage, OperationStatus.FAILURE,
				null, null, cause, logContext);
	}

	public static AnaplanResponse executeActionFailure(String responseMessage,
			Throwable cause, String logContext) {
		return new AnaplanResponse(responseMessage, OperationStatus.FAILURE,
				null, null, cause, logContext);
	}

	public static AnaplanResponse exportSuccess(String responseMessage,
			ServerFile exportOutput, ExportMetadata exportMetadata,
			String logContext) throws IllegalArgumentException {
		if (exportOutput == null) {
			LogUtil.error(logContext, "discarding response for task"
					+ responseMessage);
			throw new IllegalArgumentException(
					"Output cannot be null for a successful export");
		} else {
			return new AnaplanResponse(responseMessage,
					OperationStatus.SUCCESS, exportOutput, exportMetadata,
					null, logContext);
		}
	}

	public static AnaplanResponse importSuccess(String responseMessage,
			String logContext) {
		return new AnaplanResponse(responseMessage, OperationStatus.SUCCESS,
				null, null, null, logContext);
	}

	public static AnaplanResponse executeActionSuccess(String responseMessage,
			String logContext) {
		return new AnaplanResponse(responseMessage, OperationStatus.SUCCESS,
				null, null, null, logContext);
	}

	public static AnaplanResponse importWithFailureDump(String responseMessage,
			ServerFile failDump, String logContext) {
		return new AnaplanResponse(responseMessage,
				OperationStatus.APPLICATION_ERROR, failDump, null, null,
				logContext);
	}

	public OperationStatus getStatus() {
		return status;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public ServerFile getServerFile() {
		return serverFile;
	}

	public ExportMetadata getExportMetadata() {
		return exportMetadata;
	}

	public String getLogContext() {
		return logContext;
	}

	public Throwable getException() {
		return exception;
	}

	/**
	 * @param responseMessage
	 * @param status
	 * @param serverFile
	 * @param exportMetaData
	 * @param failureCause
	 * @param logContext
	 */
	private AnaplanResponse(String responseMessage, OperationStatus status,
			ServerFile serverFile, ExportMetadata exportMetaData,
			Throwable failureCause, String logContext) {
		this.responseMessage = responseMessage;
		this.status = status;
		this.serverFile = serverFile;
		this.exportMetadata = exportMetaData;
		this.exception = failureCause;
		this.logContext = logContext;

		LogUtil.status(logContext, "created " + this.toString());
	}

	/**
	 *
	 * @param cellReader
	 * @param isStreaming
	 * @param logContext
	 * @return The string response.
	 * @throws AnaplanAPIException
	 * @throws IOException
	 */
	private String writeResponse(CellReader cellReader, boolean isStreaming,
			String logContext) throws AnaplanAPIException, IOException {

		final StringBuffer sb = new StringBuffer();
		final String header = cellReader.getWholeHeaderRow() + "\n";
		LogUtil.debug(logContext, header);

		// write response to string-buffer
		String dataLine = cellReader.readWholeDataRow();
		while (dataLine != null) {
			dataLine += "\n";
			LogUtil.trace(logContext, dataLine);
			sb.append(dataLine);
			dataLine = cellReader.readWholeDataRow();
		}
		LogUtil.debug(logContext, "finished writing file");
		return sb.toString();
	}

	/**
	 * Includes finalising result.
	 *
	 * Currently always sets streaming=true.
	 *
	 * @param input
	 * @param serverFile
	 * @param resp
	 * @param response
	 * @param logContext
	 * @param userLog
	 *            optional log to write status and errors into.
	 * @throws IOException
	 * @throws AnaplanAPIException
	 */
	private String responseServerFile(ServerFile serverFile, String logContext)
			throws IOException, AnaplanAPIException {
		if (serverFile == null) {
			// userLog.status(UserMessages.getMessage("missingFile"));
			// response.addResult(input, getStatus(), getResponseMessage(),
			// getStatus().name(), null);
			throw new AnaplanAPIException("Response is empty: " + getStatus());
		}

		final CellReader cellReader = serverFile.getDownloadCellReader();
		if (getExportMetadata() != null) {
			LogUtil.debug(logContext, getExportMetadata()
					.collectExportFileInfo());
		}
		return writeResponse(cellReader, true, logContext);
	}

	/**
	 * Logs failure with the given reason to everywhere for all data items in
	 * the request.
	 *
	 * @param response
	 * @param request
	 * @param connection
	 * @param reason
	 */
	public static void responseFail(AnaplanConnection connection, String reason) {
		// response.addCombinedResult(request, OperationStatus.FAILURE, null,
		// reason, PayloadUtil.toPayload(reason));

		// for (TrackedData inputData : request) {
		// final UserLog dataLog = new LogUtil.ShapeDataLog(inputData,
		// connection);
		// dataLog.error("Aborting operation: " + reason);
		// }

		// final UserLog operationLog = new LogUtil.ProcessLog(response,
		// connection);
		// operationLog.error("Aborting operation for all documents in request: "
		// + reason);

		LogUtil.error(connection.getLogContext(), "Aborting operation for all "
				+ "documents in request: " + reason);
	}

	/**
	 * Logs failure with the given reason to everywhere for all data items in
	 * the request.
	 *
	 * @param response
	 * @param request
	 * @param connection
	 * @param reason
	 */
	// public static void responseFail(OperationResponse response,
	// DeleteRequest request, AnaplanConnection connection, String reason) {
	// response.addCombinedResult(request, OperationStatus.FAILURE, null,
	// reason, PayloadUtil.toPayload(reason));
	//
	// for (TrackedData inputData : request) {
	// final UserLog dataLog = new LogUtil.ShapeDataLog(inputData,
	// connection);
	// dataLog.error("Aborting operation: " + reason);
	// }
	//
	// final UserLog operationLog = new LogUtil.ProcessLog(response,
	// connection);
	// operationLog.error("Aborting Delete Operation due to : " + reason);
	// }

	/**
	 * Logs failure with the given reason to everywhere.
	 *
	 * @param response
	 * @param inputData
	 * @param connection
	 * @param reason
	 */
	// public static void responseFail(OperationResponse response,
	// TrackedData inputData, AnaplanConnection connection, String reason) {
	//
	// response.addResult(inputData, OperationStatus.FAILURE,
	// OperationStatus.FAILURE.toString(), reason,
	// PayloadUtil.toPayload(reason));
	//
	// final UserLog operationLog = new LogUtil.ProcessLog(response,
	// connection);
	// operationLog.error("Aborting operation: " + reason);
	//
	// final UserLog dataLog = new LogUtil.ShapeDataLog(inputData, connection);
	// dataLog.error("Aborting operation: " + reason);
	// }

	public static void responseEpicFail(AnaplanConnection connection,
			Throwable e, String reason) {
		final String msg;
		if (reason == null) {
			msg = "Unexpected operation error: Generating OperationResponse error for "
					+ e.getMessage();
		} else {
			msg = reason + ": " + e.getMessage();
		}

		LogUtil.error(connection.getLogContext(), msg, e); // for stack trace
		// ResponseUtil.addExceptionFailure(response, inputData, e);
	}

	// private void responseSuccess(OperationResponse response,
	// TrackedData inputData, String... messageLines) {
	// response.addResult(inputData, OperationStatus.SUCCESS,
	// this.getResponseMessage(), OperationStatus.SUCCESS.toString(),
	// PayloadUtil.toPayload(AnaplanUtil.squish(messageLines)));
	// }

	// public void writeImportData(AnaplanConnection connection,
	// TrackedData input, OperationResponse response, String importId,
	// UserLog operationLog) throws IOException, AnaplanAPIException {
	//
	// if (getServerFile() != null) {
	// responseServerFile(input, getServerFile(), response,
	// getLogContext(), operationLog);
	// } else if (getStatus() == OperationStatus.SUCCESS) {
	// responseSuccess(response, input,
	// UserMessages.getMessage("importSuccess", importId),
	// getResponseMessage());
	// } else {
	// if (getException() == null) {
	// responseFail(response, input, connection, getResponseMessage());
	// } else {
	// responseEpicFail(response, input, connection, getException(),
	// getResponseMessage());
	// }
	// }
	// }

	// public void writeExecuteActionData(AnaplanConnection connection,
	// TrackedData input, OperationResponse response, String actionId,
	// UserLog operationLog) throws AnaplanAPIException {
	//
	// operationLog
	// .error("Inside executeAction.writeExecuteAction and Operation Status is::");
	// if (getStatus() == OperationStatus.SUCCESS) {
	// responseSuccess(response, input,
	// UserMessages.getMessage("executeActionSuccess", actionId),
	// getResponseMessage());
	// } else {
	// if (getException() == null) {
	// responseFail(response, input, connection, getResponseMessage());
	// } else {
	// responseEpicFail(response, input, connection, getException(),
	// getResponseMessage());
	// }
	// }
	// }

	/**
	 *
	 * @param connection
	 * @param exportId
	 * @param logContext
	 * @throws AnaplanAPIException
	 * @throws IOException
	 */
	public String writeExportData(AnaplanConnection connection,
			String exportId, String logContext) throws AnaplanAPIException,
			IOException {
		if (getStatus() != OperationStatus.SUCCESS) {
			if (getException() == null) {
				responseFail(connection, getResponseMessage());
			} else {
				responseEpicFail(connection, getException(),
						getResponseMessage());
			}
		}
		return responseServerFile(getServerFile(), getLogContext());
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("AnaplanResponse with status ");
		sb.append(this.status == null ? "null" : this.status.toString());

		sb.append("; message: ");
		sb.append(this.responseMessage == null ? "null" : this.responseMessage);

		sb.append("; serverfile ");
		sb.append(this.serverFile == null ? "null" : this.serverFile.getName());

		sb.append("; exportmetadata: ");
		sb.append(this.exportMetadata == null ? "null" : this.exportMetadata
				.collectExportFileInfo());

		return sb.toString();
	}
}
