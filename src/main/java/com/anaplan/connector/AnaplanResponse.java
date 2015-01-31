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

package com.anaplan.connector;

import java.io.IOException;
import java.io.Serializable;


import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.CellReader;
import com.anaplan.client.ExportMetadata;
import com.anaplan.client.ServerFile;
import com.anaplan.connector.exceptions.AnaplanOperationException;
import com.anaplan.connector.utils.LogUtil;
import com.anaplan.connector.utils.OperationStatus;
import com.anaplan.connector.utils.UserMessages;


/**
 * Translates Anaplan task results into connector friendly responses. Provides
 * constructors corresponding to the expected states of a completed Anaplan
 * server task, utilities to convert these into DI platform responses.
 *
 * Payload generated here form the data output from the Anaplan connector. In
 * the case of an operation success or partial success, this is written out as
 * Current Data for a downstream connector to consume; in the case of complete
 * failure an exception is thrown to force the data-flow to stop.
 */
public class AnaplanResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String responseMessage;
	private final ServerFile serverFile;
	private final ExportMetadata exportMetadata;
	private final OperationStatus status;
	private final String logContext;
	private final Throwable exception;

	/**
	 * Failure response constructor for data exports from Anaplan, whenever a
	 * failure is encountered during a data-export. Most likely thrown due to
	 * invalid Workspace-ID, Model-ID or Export-action-ID.
	 *
	 * @param responseMessage
	 * @param exportMetadata
	 * @param cause
	 * @param logContext
	 * @return
	 */
	public static AnaplanResponse exportFailure(String responseMessage,
			ExportMetadata exportMetadata, Throwable cause, String logContext) {
		return new AnaplanResponse(responseMessage, OperationStatus.FAILURE,
				null, exportMetadata, cause, logContext);
	}

	/**
	 * Failure response constructor used to signal a failure during an import
	 * operation into Anaplan, which usually is because of malformed input data.
	 *
	 * @param responseMessage
	 * @param cause
	 * @param logContext
	 * @return
	 */
	public static AnaplanResponse importFailure(String responseMessage,
			Throwable cause, String logContext) {
		return new AnaplanResponse(responseMessage, OperationStatus.FAILURE,
				null, null, cause, logContext);
	}

	/**
	 * Failure response constructor from the connector whenever a generic
	 * execute operation fails.
	 *
	 * @param responseMessage
	 * @param cause
	 * @param logContext
	 * @return
	 */
	public static AnaplanResponse executeActionFailure(String responseMessage,
			Throwable cause, String logContext) {
		return new AnaplanResponse(responseMessage, OperationStatus.FAILURE,
				null, null, cause, logContext);
	}

	/**
	 * Success response constructor whenever an export operation succeeds.
	 *
	 * @param responseMessage
	 * @param exportOutput
	 * @param exportMetadata
	 * @param logContext
	 * @return
	 * @throws IllegalArgumentException
	 */
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

	/**
	 * Success response constructor to signal a successful Import operation.
	 *
	 * @param responseMessage
	 * @param logContext
	 * @param serverFile
	 * @return
	 */
	public static AnaplanResponse importSuccess(String responseMessage,
			String logContext, ServerFile serverFile) {
		return new AnaplanResponse(responseMessage, OperationStatus.SUCCESS,
				serverFile, null, null, logContext);
	}

	/**
	 * Success response constructor to signal a successful generic action
	 * operation, such as a successful Delete operation or an M2M operation.
	 *
	 * @param responseMessage
	 * @param logContext
	 * @return
	 */
	public static AnaplanResponse executeActionSuccess(String responseMessage,
			String logContext) {
		return new AnaplanResponse(responseMessage, OperationStatus.SUCCESS,
				null, null, null, logContext);
	}

	/**
	 * Response constructor used when an Import operation fails and the server
	 * provides a failure-dump to help debug.
	 *
	 * @param responseMessage
	 * @param failDump
	 * @param logContext
	 * @return
	 */
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
	 * Constructor.
	 *
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
	 * Uses the server cell-reader handler to read the server response contents
	 * and then write it to a string-buffer to be returned as a response.
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
		sb.append(header);
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
	 * Finalizes the result sent back from the server and returns it as a
	 * string.
	 *
	 * @param serverFile
	 * @param logContext
	 * @throws IOException
	 * @throws AnaplanAPIException
	 */
	private String responseServerFile(ServerFile serverFile, String logContext)
			throws IOException, AnaplanAPIException {
		if (serverFile == null) {
			throw new AnaplanAPIException("Response is empty: " + getStatus());
		}
		final CellReader cellReader = serverFile.getDownloadCellReader();
		if (getExportMetadata() != null) {
			LogUtil.debug(logContext,
					getExportMetadata().collectExportFileInfo());
		}
		return writeResponse(cellReader, true, logContext);
	}

	/**
	 * Currently logs the error provided in 'reason' and creates a general
	 * Log4j.error() message.
	 *
	 * @param response
	 * @param request
	 * @param connection
	 * @param reason
	 */
	public static void responseFail(AnaplanConnection connection, String reason) {
		LogUtil.error(connection.getLogContext(), "Aborting operation for all "
				+ "documents in request: " + reason);
	}

	/**
	 * Usually as a last resort to error-logging, whenever the cause of the
	 * error is unknown and everything needs to be brought to a grinding halt.
	 * In order to stop the flow, the Throwable is wrapped into a
	 * AnaplanOperationException.
	 *
	 * @param connection
	 * @param e
	 * @param reason
	 * @throws AnaplanOperationException
	 * @throws Throwable
	 */
	public static void responseEpicFail(AnaplanConnection connection,
			Throwable e, String reason) throws AnaplanOperationException {
		final String msg;
		if (reason == null) {
			msg = "Unexpected operation error: Generating OperationResponse "
					+ "error for " + e.getMessage();
		} else {
			msg = reason + ": " + e.getMessage();
		}

		LogUtil.error(connection.getLogContext(), msg, e); // for stack trace
		throw new AnaplanOperationException(e.getMessage());
	}

	/**
	 * Writes the import data to Anaplan using the provided Import-ID and
	 * connection.
	 *
	 * @param connection
	 * @param importId
	 * @param logContext
	 * @throws IOException
	 * @throws AnaplanAPIException
	 * @throws Throwable
	 */
	public void writeImportData(AnaplanConnection connection, String importId,
								String logContext)
										throws AnaplanOperationException,
											   IOException,
											   AnaplanAPIException {
		if (getServerFile() != null) {
			responseServerFile(getServerFile(), getLogContext());
		} else if (getStatus() == OperationStatus.SUCCESS) {
			LogUtil.status(UserMessages.getMessage("importSuccess", importId),
					getResponseMessage());
		} else {
			if (getException() == null) {
				responseFail(connection, getResponseMessage());
			} else {
				responseEpicFail(connection, getException(),
						getResponseMessage());
			}
		}
	}

	/**
	 * Reads the export-data from the registered Serverfile object and emits
	 * the data as a string into the Mulesoft platform for the next
	 * connector down the data-flow pipeline.
	 *
	 * @param connection
	 * @param exportId
	 * @param logContext
	 * @throws AnaplanAPIException
	 * @throws IOException
	 * @throws AnaplanOperationException
	 * @throws Throwable
	 */
	public String writeExportData(AnaplanConnection connection, String exportId,
								  String logContext)
										  throws IOException,
										  		 AnaplanAPIException,
										  		 AnaplanOperationException {
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
