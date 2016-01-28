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
import com.anaplan.connector.utils.OperationStatus;
import com.anaplan.connector.utils.UserMessages;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;


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

    private static Logger logger = LogManager.getLogger(MulesoftAnaplanResponse.class.getName());

    private static final long serialVersionUID = 1L;
    private final String responseMessage;
    private final ServerFile serverFile;
    private final ExportMetadata exportMetadata;
    private final OperationStatus status;
    private final Throwable exception;

    /**
     * Constructor.
     *
     * @param responseMessage Response message, usually export data.
     * @param status status object.
     * @param serverFile ServerFile object returned from API.
     * @param exportMetaData Meta data for Export data.
     * @param failureCause Any server error during exporting data.
     */
    private MulesoftAnaplanResponse(String responseMessage, OperationStatus status,
            ServerFile serverFile, ExportMetadata exportMetaData,
            Throwable failureCause) {
        this.responseMessage = responseMessage;
        this.status = status;
        this.serverFile = serverFile;
        this.exportMetadata = exportMetaData;
        this.exception = failureCause;

        logger.info("Created {}", this);
    }

    /** Getters */
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

    public Throwable getException() {
        return exception;
    }

    /**
     * Uses the server cell-reader handler to read the server response contents
     * and then write it to a string-buffer to be returned as a response.
     *
     * TODO: Use CSVPrinter to create the response. Check
     * DO_NOT_REBASE_writeResponseOptimization for suggested changes.
     *
     * @param cellReader CellReader object to write server response.
     * @return The string response.
     * @throws AnaplanAPIException Exception thrown when reading rows of data.
     * @throws IOException Exception thrown when reading rows of data.
     */
    private String writeResponse(CellReader cellReader)
            throws AnaplanAPIException, IOException {

        StringBuilder sb = new StringBuilder();
        final String header = StringUtils.join(cellReader.getHeaderRow(), ',');
        sb.append(header);
        logger.debug("{}", header);

        String dataLine = StringUtils.join(cellReader.readDataRow(), ',');

        String[] dataLineArr;
        while (dataLine != null) {
            sb.append("\n").append(dataLine);
            dataLineArr = cellReader.readDataRow();
            dataLine = (dataLineArr == null) ? null
                    : StringUtils.join(dataLineArr, ',');
        }
        logger.debug("finished writing file");
        return sb.toString();
    }

    /**
     * Finalizes the result sent back from the server and returns it as a
     * string.
     *
     * @param serverFile ServerFile object.
     * @throws IOException Exception thrown when reading rows of data.
     * @throws AnaplanAPIException Exception thrown when reading rows of data.
     */
    private String responseServerFile(ServerFile serverFile) throws IOException,
                AnaplanAPIException {
        if (serverFile == null) {
            throw new AnaplanAPIException("Response is empty: " + getStatus());
        }
        final CellReader cellReader = serverFile.getDownloadCellReader();
        return writeResponse(cellReader);
    }

    /**
     * Writes the import data to Anaplan using the provided Import-ID and
     * connStrategy.
     *
     * @param connection Anaplan API connection object.
     * @param importId Import action ID.
     * @throws AnaplanOperationException When server-file API object fails to import.
     * @throws IOException When server-file API object fails to import.
     * @throws AnaplanAPIException When server-file API object fails to import.
     */
    public void writeImportData(AnaplanConnection connection, String importId)
            throws AnaplanOperationException,
            IOException,
            AnaplanAPIException {
        if (getServerFile() != null) {
            responseServerFile(getServerFile());
        } else if (getStatus() == OperationStatus.SUCCESS) {
            logger.info("{}: {}", UserMessages.getMessage(
					"importSuccess", importId), getResponseMessage());
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
     * @param connection Anaplan API connection object.
     * @throws IOException IO exception
     * @throws AnaplanAPIException
     * @throws AnaplanOperationException
     */
    public String writeExportData(AnaplanConnection connection)
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
        return responseServerFile(getServerFile());
    }

    @Override
    public String toString() {
        StringBuilder strBuffer = new StringBuilder();
        strBuffer.append("AnaplanResponse with status ");
        strBuffer.append(this.status == null ? "null" : this.status.toString());

        strBuffer.append("; message: ");
        strBuffer.append(this.responseMessage == null ? "null" : this.responseMessage);

        strBuffer.append("; serverfile ");
        strBuffer.append(this.serverFile == null ? "null" : this.serverFile.getName());

        return strBuffer.toString();
    }

    /**
     * Currently logs the error provided in 'reason' and creates a general
     * Log4j.error() message.
     *
     * @param connection Anaplan API connection
     * @param reason Reason description for failed response.
     */
    public static void responseFail(AnaplanConnection connection, String reason) {
        logger.error("Aborting operation for all documents in request: {}",
				reason);
    }

    /**
     * Usually as a last resort to error-logging, whenever the cause of the
     * error is unknown and everything needs to be brought to a grinding halt.
     * In order to stop the flow, the Throwable is wrapped into a
     * AnaplanOperationException.
     *
     * @param connection Anaplan API connection.
     * @param e Throwable object containing Failure info
     * @param reason Reason for Epic Fail
     * @throws AnaplanOperationException Internal operation exception for
     *      signalling epic fail.
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

        logger.error(msg, e); // for stack trace
        throw new AnaplanOperationException(e.getMessage());
    }

    /**
     * Success response constructor whenever an export operation succeeds.
     *
     * @param responseMessage Response message from server for succesful export.
     * @param exportOutput Export output ServerFile object.
     * @param exportMetadata Export metadata.
     * @return Response object containing export data and details.
     * @throws IllegalArgumentException Thrown if null export output exists.
     */
    public static MulesoftAnaplanResponse exportSuccess(String responseMessage,
            ServerFile exportOutput, ExportMetadata exportMetadata)
                    throws IllegalArgumentException {
        if (exportOutput == null) {
            logger.error("Discarding response for task: {}", responseMessage);
            throw new IllegalArgumentException("Output cannot be null for a " +
                    "successful export");
        } else {
            return new MulesoftAnaplanResponse(responseMessage,
                    OperationStatus.SUCCESS, exportOutput, exportMetadata,
                    null);
        }
    }

    /**
     * Failure response constructor for data exports from Anaplan, whenever a
     * failure is encountered during a data-export. Most likely thrown due to
     * invalid Workspace-ID, Model-ID or Export-action-ID.
     *
     * @param responseMessage Response message for export failure.
     * @param exportMetadata Metadata for export/
     * @param cause Throwable with info regarding export failure.
     * @return Response object containing server details of Export failure.
     */
    public static MulesoftAnaplanResponse exportFailure(String responseMessage,
            ExportMetadata exportMetadata, Throwable cause) {
        return new MulesoftAnaplanResponse(responseMessage, OperationStatus.FAILURE,
                null, exportMetadata, cause);
    }

    /**
     * Success response constructor to signal a successful Import operation.
     *
     * @param responseMessage Response message from import success.
     * @param serverFile Object containing details of import.
     * @return Response object containing all response details regarding import.
     */
    public static MulesoftAnaplanResponse importSuccess(String responseMessage,
            ServerFile serverFile) {
        return new MulesoftAnaplanResponse(responseMessage, OperationStatus.SUCCESS,
                serverFile, null, null);
    }

    /**
     * Failure response constructor used to signal a failure during an import
     * operation into Anaplan, which usually is because of malformed input data.
     *
     * @param responseMessage Response message from Import failure.
     * @param cause Throwable with info regarding import failure.
     * @return Response object containing server details of Import failure.
     */
    public static MulesoftAnaplanResponse importFailure(String responseMessage,
            Throwable cause) {
        return new MulesoftAnaplanResponse(responseMessage, OperationStatus.FAILURE,
                null, null, cause);
    }

    /**
     * Response constructor used when an Import operation fails and the server
     * provides a failure-dump to help debug.
     *
     * @param responseMessage Response message from Import failure.
     * @param failDump Failure dump log from server for debugging purposes.
     * @return Response object containing server details of import failure.
     */
    public static MulesoftAnaplanResponse importWithFailureDump(String responseMessage,
            ServerFile failDump) {
        return new MulesoftAnaplanResponse(responseMessage,
                OperationStatus.APPLICATION_ERROR, failDump, null, null);
    }

    /**
     * Success response constructor to signal a successful generic action
     * operation, such as a successful Delete operation or an M2M operation.
     *
     * @param responseMessage Success response message from Execute-Action.
     * @return Response object containing execute-action success details.
     */
    public static MulesoftAnaplanResponse executeActionSuccess(String responseMessage) {
        return new MulesoftAnaplanResponse(responseMessage, OperationStatus.SUCCESS,
                null, null, null);
    }

    /**
     * Failure response constructor from the connector whenever a generic
     * execute operation fails.
     *
     * @param responseMessage Failure response message from Execute-Action.
     * @param cause Throwable with failed execute action details.
     * @return Response object containing failed execute-action details.
     */
    public static MulesoftAnaplanResponse executeActionFailure(String responseMessage,
            Throwable cause) {
        return new MulesoftAnaplanResponse(responseMessage, OperationStatus.FAILURE,
                null, null, cause);
    }

    /**
     * Success response constructor from the connector whenever a Process operation
     * succeeds on the server.
     *
     * @param responseMessage Success response message from running Process.
     * @return Response object containing Process success response details.
     */
    public static MulesoftAnaplanResponse runProcessSuccess(String responseMessage) {
        return new MulesoftAnaplanResponse(responseMessage, OperationStatus.SUCCESS,
                null, null, null);
    }

    /**
     * Failure response constructor from the connector whenever a Process
     * operation fails on the server.
     *
     * @param responseMessage Failure response message from server.
     * @param cause Throwable containing failure details.
     * @return Response object containing Process failure response details.
     */
    public static MulesoftAnaplanResponse runProcessFailure(String responseMessage,
            Throwable cause) {
        return new MulesoftAnaplanResponse(responseMessage, OperationStatus.FAILURE,
                null, null, cause);
    }

	/**
	 * Fetches dump file contents from Server response if any, otherwise returns
	 * an empty string.
	 *
	 * TODO: Move this to Anaplan-Connect.
	 *
	 * @return Response message from Dump-file.
	 */
	public String getDumpFileContents() {
		StringBuilder dumpFileContents = new StringBuilder();
		if (this.serverFile != null) {
			InputStream dumpFileStream;
			try {
				dumpFileStream = serverFile.getDownloadStream();
				if (dumpFileStream != null) {
					byte[] buffer = new byte[5];
					int read;
					do {
						if ((read = dumpFileStream.read(buffer)) > 0) {
							dumpFileContents.append(new String(buffer));
						}
					} while (read != -1);
				}
			} catch (IOException | AnaplanAPIException e) {
				logger.info("No Dump file found. Proceeding...");
			}
		}
		return dumpFileContents.toString();
	}
}
