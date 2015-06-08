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

import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.CellReader;
import com.anaplan.client.ExportMetadata;
import com.anaplan.client.ServerFile;
import com.anaplan.connector.connection.AnaplanConnection;
import com.anaplan.connector.exceptions.AnaplanOperationException;
import com.anaplan.connector.utils.LogUtil;
import com.anaplan.connector.utils.OperationStatus;
import com.anaplan.connector.utils.UserMessages;

import java.io.*;


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
public class MulesoftAnaplanResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String responseMessage;
	private final ServerFile serverFile;
	private final ExportMetadata exportMetadata;
	private final OperationStatus status;
	private final String logContext;
	private final Throwable exception;

	/**
	 * Constructor.
	 *
	 * @param responseMessage, Mulesoft Anaplan response object with details.
	 * @param status, Status of fetching an Anaplan response from the API.
	 * @param serverFile, Server-file object containing the file-upload details.
	 * @param exportMetaData, Export meta-data
	 * @param failureCause, Exception object details sent from Anaplan API.
	 * @param logContext, Log context for printing Anaplan response messages.
	 */
	private MulesoftAnaplanResponse(String responseMessage, OperationStatus status,
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
	 * @param cellReader, Cell reader object containing server response contents.
	 * @param logContext, Log context for AnaplanResponse.
	 * @return The string response.
	 * @throws AnaplanAPIException
	 * @throws IOException
	 */
	private OutputStream writeResponse(CellReader cellReader, String logContext)
			throws AnaplanAPIException, IOException {

		BufferedOutputStream bos = null;
		OutputStream outStream = null;

		try {
			outStream = new ByteArrayOutputStream();
			bos = new BufferedOutputStream(outStream);

			String header = cellReader.getWholeHeaderRow() + "\n";
			LogUtil.debug(logContext, header);

			// write response to string-buffer
			bos.write(header.getBytes());
			String dataLine = cellReader.readWholeDataRow();
			do {
				dataLine += '\n';
				LogUtil.trace(logContext, dataLine);
				bos.write(dataLine.getBytes());
				dataLine = cellReader.readWholeDataRow();
			} while (dataLine != null);

			LogUtil.debug(logContext, "finished writing file");
		} catch (IOException e) {
			LogUtil.error(logContext, e.toString());
		} finally {
			if (bos != null)
				bos.close();
			if (outStream != null)
				outStream.close();
		}
		return outStream;
	}

	/**
	 * Finalizes the result sent back from the server and returns it as a
	 * string.
	 *
	 * @param serverFile, Server-file object containing file upload details.
	 * @param logContext, Log context for AnaplanResponse log messages.
	 * @throws IOException
	 * @throws AnaplanAPIException
	 */
	private OutputStream responseServerFile(ServerFile serverFile, String logContext)
			throws IOException, AnaplanAPIException {
		if (serverFile == null) {
			throw new AnaplanAPIException("Response is empty: " + getStatus());
		}
		final CellReader cellReader = serverFile.getDownloadCellReader();
		if (getExportMetadata() != null) {
			LogUtil.debug(logContext,
					getExportMetadata().collectExportFileInfo());
		}
		return writeResponse(cellReader, logContext);
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
	 * Writes the import data to Anaplan using the provided Import-ID and
	 * connStrategy.
	 *
	 * @param connection, Anaplan connection object.
	 * @param importId, Anaplan Import ID.
	 * @param logContext, Anaplan Log context for AnaplanResponse.
	 * @throws AnaplanOperationException
	 * @throws IOException
	 * @throws AnaplanAPIException
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
	 * @param connection, Anaplan connection object.
	 * @throws AnaplanAPIException
	 * @throws IOException
	 * @throws AnaplanOperationException
	 */
	public OutputStream writeExportData(AnaplanConnection connection)
			throws IOException, AnaplanAPIException, AnaplanOperationException {
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

	/**
	 * Currently logs the error provided in 'reason' and creates a general
	 * Log4j.error() message.
	 *
	 * @param connection, Anaplan connection object.
	 * @param reason, Reason for logging a failure message.
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
	 * @param connection, Anaplan connection object.
	 * @param e, Throwable exception object.
	 * @param reason, Epic fail reason.
	 * @throws AnaplanOperationException
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
	 * Success response constructor whenever an export operation succeeds.
	 *
	 * @param responseMessage, Export success response message from Anaplan API.
	 * @param exportOutput, Export output Server-file object
	 * @param exportMetadata, Export meta-data object.
	 * @param logContext, Log context for Export success messages.
	 * @return The MulesoftAnaplanResponse
	 * @throws IllegalArgumentException
	 */
	public static MulesoftAnaplanResponse exportSuccess(String responseMessage,
			ServerFile exportOutput, ExportMetadata exportMetadata,
			String logContext) throws IllegalArgumentException {
		if (exportOutput == null) {
			LogUtil.error(logContext, "discarding response for task"
					+ responseMessage);
			throw new IllegalArgumentException(
					"Output cannot be null for a successful export");
		} else {
			return new MulesoftAnaplanResponse(responseMessage,
					OperationStatus.SUCCESS, exportOutput, exportMetadata,
					null, logContext);
		}
	}

	/**
	 * Failure response constructor for data exports from Anaplan, whenever a
	 * failure is encountered during a data-export. Most likely thrown due to
	 * invalid Workspace-ID, Model-ID or Export-action-ID.
	 *
	 * @param responseMessage, Export failure response message.
	 * @param exportMetadata, Export meta-data information.
	 * @param cause, Cause of export failure sent from server.
	 * @param logContext, Log-context for Export failure log messages.
	 * @return Mulesoft Anaplan response object with export failure details.
	 */
	public static MulesoftAnaplanResponse exportFailure(String responseMessage,
			ExportMetadata exportMetadata, Throwable cause, String logContext) {
		return new MulesoftAnaplanResponse(responseMessage, OperationStatus.FAILURE,
				null, exportMetadata, cause, logContext);
	}

	/**
	 * Success response constructor to signal a successful Import operation.
	 *
	 * @param responseMessage, Import success response message.
	 * @param logContext, Log context for successful import.
	 * @param serverFile, Import success server file object.
	 * @return Mulesoft Anaplan response object with Import success details.
	 */
	public static MulesoftAnaplanResponse importSuccess(String responseMessage,
			String logContext, ServerFile serverFile) {
		return new MulesoftAnaplanResponse(responseMessage, OperationStatus.SUCCESS,
				serverFile, null, null, logContext);
	}

	/**
	 * Failure response constructor used to signal a failure during an import
	 * operation into Anaplan, which usually is because of malformed input data.
	 *
	 * @param responseMessage, Import failure message from server.
	 * @param cause, Exception cause.
	 * @param logContext, Log-context for import failure logs.
	 * @return Mulesoft Anaplan response object with import failure details.
	 */
	public static MulesoftAnaplanResponse importFailure(String responseMessage,
			Throwable cause, String logContext) {
		return new MulesoftAnaplanResponse(responseMessage, OperationStatus.FAILURE,
				null, null, cause, logContext);
	}

	/**
	 * Response constructor used when an Import operation fails and the server
	 * provides a failure-dump to help debug.
	 *
	 * @param responseMessage, Import failure message from server.
	 * @param failDump, Fail dump ServerFile object with file-handler details.
	 * @param logContext, Log context for import failure logs.
	 * @return Mulesoft Anaplan response object with import failure details.
	 */
	public static MulesoftAnaplanResponse importWithFailureDump(String responseMessage,
			ServerFile failDump, String logContext) {
		return new MulesoftAnaplanResponse(responseMessage,
				OperationStatus.APPLICATION_ERROR, failDump, null, null,
				logContext);
	}

	/**
	 * Success response constructor to signal a successful generic action
	 * operation, such as a successful Delete operation or an M2M operation.
	 *
	 * @param responseMessage, Execute-action success response message from server.
	 * @param logContext, Log context for execute-action success logs.
	 * @return Mulesoft Anaplan response object with execute action success
	 * 		   details.
	 */
	public static MulesoftAnaplanResponse executeActionSuccess(String responseMessage,
			String logContext) {
		return new MulesoftAnaplanResponse(responseMessage, OperationStatus.SUCCESS,
				null, null, null, logContext);
	}

	/**
	 * Failure response constructor from the connector whenever a generic
	 * execute operation fails.
	 *
	 * @param responseMessage, Execute action failure response message from server.
	 * @param cause, Exception cause of execute-action failure.
	 * @param logContext, Log context for execute-actuon failure logs.
	 * @return Mulesoft Anaplan response object with execute action failure
	 * 		   details.
	 */
	public static MulesoftAnaplanResponse executeActionFailure(String responseMessage,
			Throwable cause, String logContext) {
		return new MulesoftAnaplanResponse(responseMessage, OperationStatus.FAILURE,
				null, null, cause, logContext);
	}

	/**
	 * Success response constructor from the connector whenever a process
	 * operation succeeds (see usage in docs/).
	 *
	 * @param responseMessage, Process Actions success response message from server.
	 * @param logContext, Log context for Process action success logs.
	 * @return Mulesoft Anaplan response object with Process action success
	 * 		   details.
	 */
	public static MulesoftAnaplanResponse runProcessSuccess(String responseMessage,
			String logContext) {
		return new MulesoftAnaplanResponse(responseMessage, OperationStatus.SUCCESS,
				null, null, null, logContext);
	}

	/**
	 * Failure response constructor from the connector whenever a process
	 * operation fails.
	 *
	 * @param responseMessage, Process failure response message from server.
	 * @param cause, Server exception from Process action failure.
	 * @param logContext, Log context for Process action success logs.
	 * @return Mulesoft Anaplan response object with Process action failure
	 * 		   details.
	 */
	public static MulesoftAnaplanResponse runProcessFailure(String responseMessage,
			Throwable cause, String logContext) {
		return new MulesoftAnaplanResponse(responseMessage, OperationStatus.FAILURE,
				null, null, cause, logContext);
	}
}
