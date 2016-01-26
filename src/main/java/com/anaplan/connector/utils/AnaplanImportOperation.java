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

import com.anaplan.client.*;
import com.anaplan.connector.MulesoftAnaplanResponse;
import com.anaplan.connector.connection.AnaplanConnection;
import com.anaplan.connector.exceptions.AnaplanOperationException;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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
     * Determine the appropriate CSVFormat to be used based on provided
     * column-separator and delimiter.
     * @return CSVFormat.RFC4180 or CSVFormat.TDF.
     */
    public static CSVFormat getCsvFormat(String columnSeparator, String textDelimiter)
            throws AnaplanOperationException {

        if (columnSeparator.isEmpty()) {
            throw new AnaplanOperationException("Column-Separator " +
                    "needs to be specified!");
        }

        switch (columnSeparator) {

            case Delimiters.COMMA:
                if (textDelimiter.isEmpty()) {
                    throw new AnaplanOperationException("Text-Delimiter " +
                            "needs to be specified!");
                }
                return CSVFormat.RFC4180
                        .withDelimiter(columnSeparator.charAt(0))
                        .withQuote(textDelimiter.charAt(0));

            case Delimiters.TAB:
                return CSVFormat.TDF;

            default:
                throw new AnaplanOperationException("Only commas and tabs are " +
                        "supported column-separators!");
        }

    }

	/**
	 * Import Data Parser: splits import data by new-lines, then for each row,
	 * splits by the provided column-separator and escape delimiter.
	 *
	 * @param data
     *              Data String.
	 * @param columnSeparator
     *              Column separator character.
	 * @return
     *              Array of rows properly escaped.
	 * @throws AnaplanOperationException
     *              To capture any exception when reading in data to buffered
     *              reader object.
	 */
	private static List<String[]> parseImportData(String data, String columnSeparator,
	        String delimiter) throws AnaplanOperationException {

        if (columnSeparator.length() > 1 && !columnSeparator.equals(Delimiters.TAB)) {
            throw new IllegalArgumentException(
                    "Multi-character Column-Separator not supported!");
        }

        if (delimiter.length() > 1) {
            throw new IllegalArgumentException("Multi-character Text Delimiter "
                    + "string not supported!");
        }

        CSVFormat csvFormat = getCsvFormat(columnSeparator, delimiter);
        String[] cellTokens;
        List<String[]> rows = new ArrayList<>();
        CSVParser csvParser;

        try {
            csvParser = CSVParser.parse(data, csvFormat);
            int cellIdx;
            for (CSVRecord record : csvParser.getRecords()) {
                Iterator<String> cellIter = record.iterator();
                cellTokens = new String[record.size()];
                cellIdx = 0;
                while (cellIter.hasNext()) {
                    cellTokens[cellIdx++] = cellIter.next();
                }
                rows.add(cellTokens);
            }
        } catch (IOException e) {
            throw new AnaplanOperationException("Error parsing data:", e);
        }

        return rows;
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

		int rowsProcessed = 0;

        Import imp;
        try {
            imp = model.getImport(importId);
        } catch (AnaplanAPIException e) {
            throw new AnaplanOperationException("Error fetching Import action:", e);
        }
        if (imp == null) {
			final String msg = "Invalid import!";
			return MulesoftAnaplanResponse.importFailure(msg, null);
		}

		ServerFile serverFile;
        try {
            serverFile = model.getServerFile(imp.getSourceFileId());
            if (serverFile != null) {

                // set the column-separator and delimiter for the input
                serverFile.setSeparator(columnSeparator);
                serverFile.setDelimiter(delimiter);

                List<String[]> rows = parseImportData(data, columnSeparator,
                        delimiter);

                // get the data-writer and write data to it, i.e. serverFile by
                // reference
                final CellWriter dataWriter = serverFile.getUploadCellWriter();
                dataWriter.writeHeaderRow(rows.get(0));
                logger.info("import header is:\n" + AnaplanUtil.debugOutput(
                        rows.get(0)));

                for (String[] row : rows) {
                    dataWriter.writeDataRow(row);
                    ++rowsProcessed;
                }
                dataWriter.close();
            }
        } catch (AnaplanAPIException | IOException e) {
            throw new AnaplanOperationException("Error fetching server-file " +
                    "and writing data to it:", e);
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

        String taskDetailsMsg = collectTaskLogs(status) +
                "Import completed successfully: (" + rowsProcessed +
                " records processed)";
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
        logger.info("Workspace-ID: " + workspaceId);
        logger.info("Model-ID: " + modelId);
        logger.info("Import-ID: " + importId);

		// validate workspace-ID and model-ID are valid, else throw exception
		validateInput(workspaceId, modelId);

		try {
            logger.info("Starting import: " + importId);

			final MulesoftAnaplanResponse anaplanResponse = runImportCsv(data, model,
					importId, columnSeparator, delimiter);

			logger.info("Import complete: Status: "
					+ anaplanResponse.getStatus() + ", Response message: "
					+ anaplanResponse.getResponseMessage());

		} catch (JsonSyntaxException e) {
			MulesoftAnaplanResponse.responseEpicFail(apiConn, e, null);
		} finally {
			apiConn.closeConnection();
		}

		String statusMsg = "[" + importId + "] ran successfully!";
		logger.info(statusMsg);
		return statusMsg + "\n\n" + getRunStatusDetails();
	}
}