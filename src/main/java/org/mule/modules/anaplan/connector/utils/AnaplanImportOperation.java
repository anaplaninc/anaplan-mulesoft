/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package org.mule.modules.anaplan.connector.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.mule.modules.anaplan.connector.AnaplanConnection;
import org.mule.modules.anaplan.connector.AnaplanResponse;
import org.mule.modules.anaplan.connector.exceptions.AnaplanOperationException;

import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.CellWriter;
import com.anaplan.client.Import;
import com.anaplan.client.Model;
import com.anaplan.client.ServerFile;
import com.anaplan.client.Task;
import com.anaplan.client.TaskResult;
import com.anaplan.client.TaskResultDetail;
import com.anaplan.client.TaskStatus;
import com.google.gson.JsonSyntaxException;


/**
 * Used to import CSV data into Anaplan
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
	 * Creates the regex to split CSV lines with provided delimiter. This is
	 * esepcially useful when escape quotes are used for cell values.
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
	private static AnaplanResponse runImportCsv(String data, Model model,
			String importId, String columnSeparator, String delimiter,
			String logContext)
					throws AnaplanAPIException, JsonSyntaxException,
					IOException {

		int rowsProcessed = 0;

		final Import imp = model.getImport(importId);
		if (imp == null) {
			final String msg = "Invalid import!";
			return AnaplanResponse.importFailure(msg, null, logContext);
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

		final Task task = imp.createTask();
		final TaskStatus status = AnaplanUtil.runServerTask(task, logContext);
		final TaskResult taskResult = status.getResult();

		final StringBuilder taskDetails = new StringBuilder();
		 taskDetails.append("Import complete: (" + rowsProcessed
				 + " records processed)");

		taskDetails.append("Import complete: Successfully");
		if (taskResult.getDetails() != null) {
			for (TaskResultDetail detail : taskResult.getDetails()) {
				taskDetails.append("\n" + detail.getLocalizedMessageText());
			}
			if (status.getTaskState() == TaskStatus.State.COMPLETE
					&& status.getResult().isSuccessful()) {
				LogUtil.status(logContext, "Import complete");
			}
		}
		final String importDetails = taskDetails.toString();
		LogUtil.status(logContext, importDetails);

		if (taskResult.isFailureDumpAvailable()) {
			LogUtil.status(logContext, UserMessages.getMessage("failureDump"));
			final ServerFile failDump = taskResult.getFailureDump();

			return AnaplanResponse.importWithFailureDump(
					UserMessages.getMessage("importBadData", importId),
					failDump, logContext);
		} else {
			LogUtil.status(logContext, UserMessages.getMessage("noFailureDump"));

			if (taskResult.isSuccessful()) {
				return AnaplanResponse.importSuccess(importDetails, logContext,
						serverFile);
			} else {
				return AnaplanResponse.importFailure(importDetails, null,
						logContext);
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
	public void runImport(String data, String workspaceId, String modelId,
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

			final AnaplanResponse anaplanResponse = runImportCsv(data, model,
					importId, columnSeparator, delimiter, importLogContext);
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