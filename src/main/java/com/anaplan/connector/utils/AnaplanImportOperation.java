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

package com.anaplan.connector.utils;


import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.Import;
import com.anaplan.client.Model;
import com.anaplan.client.ServerFile;
import com.anaplan.client.Task;
import com.anaplan.client.TaskResult;
import com.anaplan.client.TaskStatus;
import com.anaplan.connector.MulesoftAnaplanResponse;
import com.anaplan.connector.connection.AnaplanConnection;
import com.anaplan.connector.exceptions.AnaplanOperationException;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;


/**
 * Used to import CSV data into Anaplan lists or modules.
 *
 * @author spondonsaha
 */
public class AnaplanImportOperation extends BaseAnaplanOperation{

    private static Logger logger = LogManager.getLogger(
            AnaplanImportOperation.class.getName());

    private Import importService;
    private ServerFile serverFile;
    private MulesoftAnaplanResponse response;

    public AnaplanImportOperation(AnaplanConnection apiConn) {
        super(apiConn);
    }

    public MulesoftAnaplanResponse getResponse() {
        return response;
    }

    public Import getImportService() {
        return importService;
    }

    public ServerFile getServerFile() {
        return serverFile;
    }

    public void setImportService(Import importService) {
        this.importService = importService;
    }

    public void setServerFile(ServerFile serverFile) {
        this.serverFile = serverFile;
    }

    public void setResponse(MulesoftAnaplanResponse response) {
        this.response = response;
    }

    /**
     * Uploads contents of dataStream, for the Serverfile for the provided
     * Import-ID and Model-ID.
     *
     * @param dataStream Import data
     * @param model Model object to which to import to
     * @param importId Import action ID
     * @throws AnaplanAPIException Thrown when Anaplan API operation fails or
     *                             error is encountered when writing to
     *                             cell data writer.
     */
    private AnaplanImportOperation upload(InputStream dataStream,
                                          Model model, String importId,
                                          Integer bufferSize)
            throws AnaplanOperationException {

        // defaults to 1MB
        bufferSize = (bufferSize == null) ? 1048576 : bufferSize;

        // Get the Import object, then use that to fetch the ServerFile, from
        // the Model, that will help create the upload-stream
        try {
            setImportService(model.getImport(importId));
        } catch (AnaplanAPIException e) {
            throw new AnaplanOperationException("Error fetching Import action:", e);
        }
        if (getImportService() == null) {
            throw new AnaplanOperationException(MessageFormat.format("Invalid " +
                    "import ID provided: {0}", importId));
        }

        try {
            setServerFile(model.getServerFile(getImportService().getSourceFileId()));
            if (getServerFile() == null) {
                throw new AnaplanOperationException("Could not fetch server-file!");
            }

            // upload the data file from one stream to the other, in provided bufferSize
            OutputStream anaplanUploadStream = getServerFile().getUploadStream();
            byte[] buffer = new byte[bufferSize];
            int len;
            int totalBytes = 0;
            while ((len = dataStream.read(buffer)) != -1) {
                if (len < bufferSize) {
                    anaplanUploadStream.write(buffer, 0, len);
                    totalBytes += len;
                } else {
                    anaplanUploadStream.write(buffer);
                    totalBytes += bufferSize;
                }
            }
            anaplanUploadStream.close();
            dataStream.close();

            logger.info("Uploaded '{}' bytes of data!", totalBytes);

        } catch (AnaplanAPIException | IOException e) {
            throw new AnaplanOperationException("Error encountered while " +
                    "importing data: ", e);
        }

        return this;
    }

    public AnaplanImportOperation runImportTask(String importId) throws AnaplanOperationException {

        Task task;
        TaskStatus status;
        try {
            task = getImportService().createTask();
            status = AnaplanUtil.runServerTask(task);
        } catch (AnaplanAPIException e) {
            throw new AnaplanOperationException("Error running Import action:", e);
        }

        // Get task status details and fetch the row counts
        String taskDetailsMsg = collectTaskLogs(status);
        setRunStatusDetails(taskDetailsMsg);
        logger.info(getRunStatusDetails());
        if (taskDetailsMsg == null) {
            logger.warn("NULL task details from API response!");
        }

        // Determine execution status and create response.
        final TaskResult taskResult = status.getResult();
        if (taskResult.isFailureDumpAvailable()) {
            logger.info(UserMessages.getMessage("failureDump"));
            final ServerFile failDump = taskResult.getFailureDump();
            setResponse(MulesoftAnaplanResponse.importWithFailureDump(
                UserMessages.getMessage("importBadData", importId),
                failDump));
        } else {
            logger.info(UserMessages.getMessage("noFailureDump"));

            if (taskResult.isSuccessful()) {
                setResponse(MulesoftAnaplanResponse.importSuccess(getRunStatusDetails(),
                    getServerFile()));
            } else {
                setResponse(MulesoftAnaplanResponse.importFailure(getRunStatusDetails(),
                    null));
            }
        }

        return this;
    }

    /**
     * Imports a model using the provided workspace-ID, model-ID and Import-ID.
     *
     * @param workspaceId Anaplan Workspace ID
     * @param modelId Anaplan Model ID
     * @param importId Anaplan Import ID
     * @throws AnaplanOperationException Internal operation exception thrown to
     *     capture any IOException, JsonSyntaxException or AnaplanAPIException.
     */
    public String runImport(InputStream data,
                            String workspaceId,
                            String modelId,
                            String importId,
                            Integer bufferSize) throws AnaplanOperationException {

        logger.info("<< Starting import >>");
        logger.info("Workspace-ID: {}", workspaceId);
        logger.info("Model-ID: {}", modelId);
        logger.info("Import-ID: {}", importId);

        // validate workspace-ID and model-ID are valid, else throw exception
        validateInput(workspaceId, modelId);

        MulesoftAnaplanResponse anaplanResponse;
        String importResponse = "";
        try {
            logger.info("Starting import: {}", importId);
            anaplanResponse = upload(data, model, importId, bufferSize)
                .runImportTask(importId)
                .getResponse();
            importResponse = createResponse(anaplanResponse);
            logger.info("Import complete: Status: {}, Response message: {}",
                    anaplanResponse.getStatus(), importResponse);
        } catch (JsonSyntaxException e) {
            MulesoftAnaplanResponse.responseEpicFail(apiConn, e, null);
        } finally {
            apiConn.closeConnection();
        }

        return importResponse;
    }
}