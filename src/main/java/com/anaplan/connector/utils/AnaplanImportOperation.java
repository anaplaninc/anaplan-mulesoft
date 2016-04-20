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
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Iterator;


/**
 * Used to import CSV data into Anaplan lists or modules.
 *
 * @author spondonsaha
 */
public class AnaplanImportOperation extends BaseAnaplanOperation{

    private static Logger logger = LogManager.getLogger(
            AnaplanImportOperation.class.getName());

    public AnaplanImportOperation(AnaplanConnection apiConn) {
        super(apiConn);
    }

    /**
     * Core method to run the Import operation. Expects data as a string-ified
     * CSV, parses the data based on provided column-separator and delimiter,
     * creates an import action based on Model provided, writes the provided
     * data to the action object, executes the action on the server and
     * monitor's the status until the import completes successfully and responds
     * the status (failed/succeeded) via an AnaplanResponse object.
     *
     * @param data Import CSV data
     * @param model Model object to which to import to
     * @param importId Import action ID
     * @param delimiter Escape character for cell values.
     * @throws AnaplanAPIException Thrown when Anaplan API operation fails or
     *                             error is encountered when writing to
     *                             cell data writer.
     */
    private static MulesoftAnaplanResponse runImportCsv(String data,
                                                        Model model,
                                                        String importId,
                                                        String columnSeparator,
                                                        String delimiter)
            throws AnaplanOperationException {

        // 1. Write the provided CSV data to the data-writer.

        Import imp;
        try {
            imp = model.getImport(importId);
        } catch (AnaplanAPIException e) {
            throw new AnaplanOperationException("Error fetching Import action:", e);
        }
        if (imp == null) {
            throw new AnaplanOperationException(MessageFormat.format("Invalid " +
                    "import ID provided: {0}", importId));
        }

        ServerFile serverFile;
        try {
            serverFile = model.getServerFile(imp.getSourceFileId());
            if (serverFile == null) {
                throw new AnaplanOperationException("Could not fetch server-file!");
            }
            // set the column-separator and delimiter for the input
            serverFile.setSeparator(columnSeparator);
            serverFile.setDelimiter(delimiter);

            // upload the data file as a stream
            OutputStream uploadStream = serverFile.getUploadStream();
            Iterator<String> iterator = AnaplanUtil.stringChunkReader(data);
            byte[] dataChunk;
            while (iterator.hasNext()) {
                dataChunk = iterator.next().getBytes("UTF-8");
                uploadStream.write(dataChunk);
            }
            uploadStream.close();

        } catch (AnaplanAPIException | IOException e) {
            throw new AnaplanOperationException("Error encountered while " +
                    "importing data: ", e);
        }

        // 2. Create the task that will import the data to the server.

        Task task;
        TaskStatus status;
        try {
            task = imp.createTask();
            status = AnaplanUtil.runServerTask(task);
        } catch (AnaplanAPIException e) {
            throw new AnaplanOperationException("Error running Import action:", e);
        }

        // 3. Get task status details and fetch the row counts
        String taskDetailsMsg = collectTaskLogs(status);
        setRunStatusDetails(taskDetailsMsg);
        logger.info(getRunStatusDetails());

        // 3. Determine execution status and create response.

        final TaskResult taskResult = status.getResult();
        if (taskResult.isFailureDumpAvailable()) {
            logger.info(UserMessages.getMessage("failureDump"));
            final ServerFile failDump = taskResult.getFailureDump();
            return MulesoftAnaplanResponse.importWithFailureDump(
                    UserMessages.getMessage("importBadData", importId),
                    failDump);
        } else {
            logger.info(UserMessages.getMessage("noFailureDump"));

            if (taskResult.isSuccessful()) {
                return MulesoftAnaplanResponse.importSuccess(getRunStatusDetails(),
                        serverFile);
            } else {
                return MulesoftAnaplanResponse.importFailure(getRunStatusDetails(),
                        null);
            }
        }
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
    public String runImport(String data,
                            String workspaceId,
                            String modelId,
                            String importId,
                            String columnSeparator,
                            String delimiter) throws AnaplanOperationException {

        logger.info("<< Starting import >>");
        logger.info("Workspace-ID: {}", workspaceId);
        logger.info("Model-ID: {}", modelId);
        logger.info("Import-ID: {}", importId);

        // validate workspace-ID and model-ID are valid, else throw exception
        validateInput(workspaceId, modelId);

        MulesoftAnaplanResponse anaplanResponse = null;
        try {
            logger.info("Starting import: {}", importId);

            anaplanResponse = runImportCsv(data, model, importId, columnSeparator,
                    delimiter);

            logger.info("Import complete: Status: {}, Response message: {}",
                    anaplanResponse.getStatus(),
                    anaplanResponse.getResponseMessage());

        } catch (JsonSyntaxException e) {
            MulesoftAnaplanResponse.responseEpicFail(apiConn, e, null);
        } finally {
            apiConn.closeConnection();
        }

        return createResponse(anaplanResponse);
    }
}