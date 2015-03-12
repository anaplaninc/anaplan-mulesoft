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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.CellWriter;
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


/**
 * Used to import CSV data into Anaplan lists or modules.
 *
 * @author spondonsaha
 */
public class AnaplanImportOperation extends BaseAnaplanOperation{

	protected static String[] HEADER;

	/**
	 * Constructor
	 * @param conn
	 */
	public AnaplanImportOperation(AnaplanConnection apiConn) {
		super(apiConn);
	}

	/**
	 * Creates the regex to split CSV lines with provided column-separator and
	 * delimiter. This is esepcially useful when escape quotes are used for
	 * cell values.
	 *
	 * @param delimiter
	 * @return
	 */
	private static String generateDelimiterRegex(String columnSeparator,
			String delimiter) {
		return columnSeparator.trim() + "(?=([^\\" + delimiter.trim() + "]*\\" +
			   delimiter.trim() + "[^\\" + delimiter.trim() + "]*\\" +
			   delimiter.trim() + ")*[^\\" + delimiter.trim() + "]*$)";
	}

	/**
	 * Import Data Parser: splits import data by new-lines, then for each row,
	 * splits by the provided column-separator and escape delimiter.
	 *
	 * @param is
	 * @param rowCount
	 * @param columnSeperator
	 * @return Array of rows properly escaped.
	 * @throws IOException
	 */
	private static List<String[]> parseImportData(InputStream is, int rowCount,
			String columnSeparator, String delimiter) throws IOException {
		String line;
		String[] cellTokens;
		List<String[]> rows = new ArrayList<String[]>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String delimRegex = generateDelimiterRegex(columnSeparator, delimiter);
		while ((line = reader.readLine()) != null) {
			// trim all quotes
			cellTokens = line.split(delimRegex);
			for (int i = 0; i < cellTokens.length; i++)
				cellTokens[i] = cellTokens[i].replaceAll("^\"|\"$", "");
			// append header and rows
			if (rowCount == 0) {
				HEADER = cellTokens;
			} else {
				rows.add(cellTokens);
			}
			rowCount++;
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
	 * @param data
	 * @param model
	 * @param importId
	 * @param delimiter
	 * @throws AnaplanAPIException
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	private static MulesoftAnaplanResponse runImportCsv(String data, Model model,
			String importId, String columnSeparator, String delimiter,
			String logContext)
					throws AnaplanAPIException, JsonSyntaxException,
					IOException {

		// 1. Write the provided CSV data to the data-writer.

		int rowsProcessed = 0;

		final Import imp = model.getImport(importId);
		if (imp == null) {
			final String msg = "Invalid import!";
			return MulesoftAnaplanResponse.importFailure(msg, null, logContext);
		}

		final ServerFile serverFile = model.getServerFile(imp.getSourceFileId());
		if (serverFile != null) {

			// set the column-separator and delimiter for the input
			serverFile.setSeparator(columnSeparator);
			serverFile.setDelimiter(delimiter);

			int rowCount = 0;
			InputStream is = new ByteArrayInputStream(data.getBytes());
			List<String[]> rows = parseImportData(is, rowCount, columnSeparator,
					delimiter);

			// get the data-writer and write data to it, i.e. serverFile by
			// reference
			final CellWriter dataWriter = serverFile.getUploadCellWriter();
			dataWriter.writeHeaderRow(HEADER);
			LogUtil.status(logContext, "import header is:\n"
					+ AnaplanUtil.debug_output(HEADER));

			for (String[] row : rows) {
				dataWriter.writeDataRow(row);
				LogUtil.trace(logContext, rowsProcessed + "-"
						+ AnaplanUtil.debug_output(row));
				++rowsProcessed;
			}
			dataWriter.close();
		}

		// 2. Create the task that will import the data to the server.

		final Task task = imp.createTask();
		final TaskStatus status = AnaplanUtil.runServerTask(task, logContext);

		final StringBuilder taskDetails = new StringBuilder();
		taskDetails.append(collectTaskLogs(status));
		taskDetails.append("Import completed successfully: (" + rowsProcessed
				 + " records processed)");
		setRunStatusDetails(taskDetails.toString());
		LogUtil.status(logContext, getRunStatusDetails());

		// 3. Determine execution status and create response.

		final TaskResult taskResult = status.getResult();
		if (taskResult.isFailureDumpAvailable()) {
			LogUtil.status(logContext, UserMessages.getMessage("failureDump"));
			final ServerFile failDump = taskResult.getFailureDump();

			return MulesoftAnaplanResponse.importWithFailureDump(
					UserMessages.getMessage("importBadData", importId),
					failDump, logContext);
		} else {
			LogUtil.status(logContext, UserMessages.getMessage("noFailureDump"));

			if (taskResult.isSuccessful()) {
				return MulesoftAnaplanResponse.importSuccess(getRunStatusDetails(),
						logContext, serverFile);
			} else {
				return MulesoftAnaplanResponse.importFailure(getRunStatusDetails(),
						null, logContext);
			}
		}
	}

	/**
	 * Imports a model using the provided workspace-ID, model-ID and Import-ID.
	 *
	 * @param workspaceId
	 * @param modelId
	 * @param importId
	 * @throws AnaplanOperationException
	 */
	public String runImport(String data, String workspaceId, String modelId,
			String importId, String columnSeparator, String delimiter)
					throws AnaplanOperationException {

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

			final MulesoftAnaplanResponse anaplanResponse = runImportCsv(data, model,
					importId, columnSeparator, delimiter, importLogContext);
			anaplanResponse.writeImportData(apiConn, importId, importLogContext);

			LogUtil.status(importLogContext, "Import complete: Status: "
					+ anaplanResponse.getStatus() + ", Response message: "
					+ anaplanResponse.getResponseMessage());

		} catch (IOException e) {
			MulesoftAnaplanResponse.responseEpicFail(apiConn, e, null);
		} catch (JsonSyntaxException e) {
			MulesoftAnaplanResponse.responseEpicFail(apiConn, e, null);
		} catch (AnaplanAPIException e) {
			MulesoftAnaplanResponse.responseEpicFail(apiConn, e, null);
		} finally {
			apiConn.closeConnection();
		}

		String statusMsg = "[" + importId + "] ran successfully!";
		LogUtil.status(importLogContext, statusMsg);
		return statusMsg + "\n\n" + getRunStatusDetails();
	}
}