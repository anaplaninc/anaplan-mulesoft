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

import org.mule.modules.anaplan.connector.AnaplanResponse;

import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.CellWriter;
import com.anaplan.client.Export;
import com.anaplan.client.Import;
import com.anaplan.client.Model;
import com.anaplan.client.ServerFile;
import com.anaplan.client.Task;
import com.anaplan.client.TaskResult;
import com.anaplan.client.TaskResultDetail;
import com.anaplan.client.TaskStatus;
import com.google.gson.JsonSyntaxException;
//import com.google.gson.JsonSyntaxException;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import com.anaplan.client.Export;
//import com.boomi.anaplan.connector.AnaplanConnection;
//import com.boomi.anaplan.connector.AnaplanResponse;
//import com.boomi.anaplan.exceptions.AnaplanConnectionException;
//import com.boomi.anaplan.util.LogUtil.UserLog;

/**
 * Utilities here handle communication with the Anaplan API
 *
 * @author spondonsaha
 */
public class AnaplanUtil {

	private AnaplanUtil() {
		// static-only
	}

	protected static String[] HEADER;

//	public static AnaplanResponse runExport(AnaplanConnection apiConn,
//			String exportId, String logContext) {
//
//		// fetch the model by opening the connection
//		Model model = null;
//		try {
//			model = apiConn.openConnection();
//			LogUtil.status(logContext, "Fetched model: " + model.getName());
//		} catch (AnaplanConnectionException e) {
//			final String msg = UserMessages.getMessage("modelAccessFail",
//					e.getMessage());
//			return AnaplanResponse.exportFailure(msg, null, e, logContext);
//		}
//
//		// run the export
//		try {
//			return doExport(model, exportId, logContext);
//		} catch (AnaplanAPIException e) {
//			final String msg = UserMessages.getMessage("apiConnectFail",
//					e.getMessage());
//			return AnaplanResponse.exportFailure(msg, null, e, logContext);
//		} catch (IOException e) {
//			final String msg = UserMessages.getMessage("apiConnectFail",
//					e.getMessage());
//			return AnaplanResponse.exportFailure(msg, null, e, logContext);
//		}
//	}

//	public static AnaplanResponse executeAction(AnaplanConnection connection,
//			String actionId, String logContext, UserLog userLog) {
//
//		Model model = null;
//		try {
//			model = connection.openConnection();
//			userLog.status(UserMessages.getMessage("modelSuccess",
//					model.getName()));
//		} catch (AnaplanConnectionException e) {
//			final String msg = UserMessages.getMessage("modelAccessFail",
//					e.getMessage());
//			return AnaplanResponse.executeActionFailure(msg, e, logContext);
//		}
//		try {
//			return executeAction(model, actionId, logContext, userLog);
//		} catch (AnaplanAPIException e) {
//			final String msg = UserMessages.getMessage("apiConnectFail",
//					e.getMessage());
//			// LOGGER.error("{} :: {} = {}", logContext, msg, e);
//			return AnaplanResponse.executeActionFailure(msg, e, logContext);
//		}
//
//	}

//	private static AnaplanResponse executeAction(Model model, String actionId,
//			String logContext, UserLog userLog) throws AnaplanAPIException {
//		final Action action = model.getAction(actionId);
//
//		if (action == null) {
//			final String msg = UserMessages.getMessage("invalidAction",
//					actionId);
//			return AnaplanResponse.executeActionFailure(msg, null, logContext);
//		}
//
//		final Task task = action.createTask();
//		final TaskStatus status = runServerTask(task, logContext);
//
//		if (status.getTaskState() == TaskStatus.State.COMPLETE
//				&& status.getResult().isSuccessful()) {
//			userLog.status("Action executed successfully");
//			return AnaplanResponse.executeActionSuccess(status.getTaskState()
//					.name(), logContext);
//		} else {
//			return AnaplanResponse.executeActionFailure(
//					"Execute Action Failed", null, logContext);
//		}
//	}

	/**
	 * Performs the Model export operation.
	 *
	 * @param model
	 * @param exportId
	 * @param logContext
	 * @return <code>AnaplanResponse</code> object.
	 * @throws IOException
	 * @throws AnaplanAPIException
	 */
	public static AnaplanResponse doExport(Model model, String exportId,
			String logContext) throws IOException, AnaplanAPIException {

		final Export exp = model.getExport(exportId);
		if (exp == null) {
			final String msg = UserMessages.getMessage("invalidExport", exportId);
			return AnaplanResponse.exportFailure(msg, null, null, logContext);
		}

		final Task task = exp.createTask();
		final TaskStatus status = runServerTask(task, logContext);

		if (status.getTaskState() == TaskStatus.State.COMPLETE &&
			status.getResult().isSuccessful()) {
			LogUtil.status(logContext, "Export complete.");
			final ServerFile serverFile = model.getServerFile(exp.getName());
			if (serverFile == null) {
				return AnaplanResponse.exportFailure(
						UserMessages.getMessage("exportRetrieveError",
								exp.getName()), exp.getExportMetadata(), null,
						logContext);
			}
			return AnaplanResponse.exportSuccess(status.getTaskState().name(),
					serverFile, exp.getExportMetadata(), logContext);
		} else {
			LogUtil.error(logContext, "Export failed !!!");
			return AnaplanResponse.exportFailure(status.getTaskState().name(),
					exp.getExportMetadata(), null, logContext);
		}
	}

	/**
	 *
	 * @param data
	 * @param model
	 * @param importId
	 * @param delimiter
	 * @throws AnaplanAPIException
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	public static AnaplanResponse runImportCsv(String data, Model model,
			String importId, String delimiter, String logContext)
					throws AnaplanAPIException, JsonSyntaxException, IOException {

		final Import imp = model.getImport(importId);
		if (imp == null) {
			final String msg = "Invalid import!";
			return AnaplanResponse.importFailure(msg, null, logContext);
		}

		final ServerFile serverFile = model.getServerFile(imp.getSourceFileId());

		if (serverFile != null) {
			 serverFile.setSeparator(delimiter);
			// final String msg = UserMessages.getMessage("invalidFile",
			// imp.getSourceFileId());
			// userLog.error(msg);
			// return AnaplanResponse.importFailure(msg, null, logContext);

			int row_count = 0;
			// List<String[]> rows = parseCsv(is,row_count);
			InputStream is = new ByteArrayInputStream(data.getBytes());
			List<String[]> rows = parseImportData(is, row_count, delimiter);

			/*
			 * final CellWriter dataWriter =
			 * serverFile.getUploadCellWriter(serverFile .getSeparator());
			 */
			final CellWriter dataWriter = serverFile.getUploadCellWriter();
			dataWriter.writeHeaderRow(HEADER);
			LogUtil.status(logContext, "import header is:\n"
					+ debug_output(HEADER));

			int rowsProcessed = 0;
			for (String[] row : rows) {
				dataWriter.writeDataRow(row);
				LogUtil.trace(logContext, rowsProcessed + "-"
						+ debug_output(row));
				++rowsProcessed;
			}
			dataWriter.close();
		}

		final Task task = imp.createTask();
		final TaskStatus status = runServerTask(task, logContext);
		final TaskResult taskResult = status.getResult();

		final StringBuilder taskDetails = new StringBuilder();
		// taskDetails.append("Import complete: (" + rowsProcessed
		// + " records processed)");

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
	 * CSV parser extracted from runImportCSV so Unit test could be created
	 *
	 * @param is
	 * @param row_count
	 * @return
	 * @throws IOException
	 */
	public static List<String[]> parseCsv(InputStream is, int row_count)
			throws IOException {
		String line;
		List<String[]> rows = new ArrayList<String[]>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		while ((line = reader.readLine()) != null) {
			if (row_count == 0) {
				setHeader(line.split(","));
			} else {
				String[] row = line.split(",");
				rows.add(row);
			}

			row_count++;
		}
		return rows;
	}

	/**
	 * Import Data Parser
	 *
	 * @param is
	 * @param row_count
	 * @param columnSeperator
	 * @return
	 * @throws IOException
	 */
	public static List<String[]> parseImportData(InputStream is, int row_count,
			String columnSeparator) throws IOException {
		String line;
		List<String[]> rows = new ArrayList<String[]>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		while ((line = reader.readLine()) != null) {
			if (row_count == 0) {
				setHeader(line.split(columnSeparator));
			} else {
				String[] row = line.split(columnSeparator);
				rows.add(row);
			}

			row_count++;
		}
		return rows;
	}

	/**
	 * @param lines
	 * @return '\n'-delimited concat of lines, no trailing newline. Returns
	 *         empty string if lines is null.
	 */
	public static String squish(String... lines) {
		if (lines == null || lines.length == 0) {
			return "";
		}

		final StringBuilder sb = new StringBuilder();
		sb.append(lines[0]);

		for (int l = 1; l < lines.length; ++l) {
			sb.append("\n");
			sb.append(lines[l]);
		}

		return sb.toString();
	}

	private static String debug_output(String[] toprint) {
		String sb = "";
		for (String s : toprint) {
			sb += s + "||";
		}
		if (sb.length() > 1) {
			return sb.substring(0, sb.length() - 1);
		} else {
			return "*";
		}
	}

	/**
	 * Executes a Anaplan import or export task.
	 *
	 * @param task
	 * @param logContext
	 * @return
	 * @throws AnaplanAPIException
	 */
	private static TaskStatus runServerTask(Task task, String logContext)
			throws AnaplanAPIException {
		TaskStatus status = task.getStatus();
		LogUtil.error(logContext, "TASK STATUS: " + status.getTaskState().toString());
		while (status.getTaskState() != TaskStatus.State.COMPLETE
				&& status.getTaskState() != TaskStatus.State.CANCELLED) {

			// if busy, nap and check again after
			try {
				Thread.sleep(1000);
				LogUtil.debug(logContext, "Running Task = "
						+ task.getStatus().getProgress());
			} catch (InterruptedException e) {
				LogUtil.error(logContext,
						"Task interrupted!\n" + e.getMessage());
			}
			status = task.getStatus();
		}

		return status;
	}

	/**
	 * @return the header
	 */
	public static String[] getHeader() {
		return HEADER;
	}

	/**
	 * @param header
	 *            the header to set
	 */
	public static void setHeader(String[] header) {
		HEADER = header;
	}

	/**
	 * Check whether workspace and model are valid, and cahe if they are.
	 * Requires a valid openConnection, i.e. cumulative with
	 * {@link #cacheService()}.
	 *
	 * @throws AnaplanConnectionException
	 *             With user-friendly error message if validation failed
	 * @throws IllegalStateException
	 *             if no open connection available
	 */
	// TODO: This needs to be in AnaplanUtil, since its workspace and model
	// related. This class needs to be only involved with authentication.
//	private void cacheWorkspaceAndModel(String workspaceId, String modelId,
//			AnaplanConnection openConnection) throws AnaplanConnectionException {
//
//		if (openConnection == null) {
//			throw new IllegalStateException(
//					"Can't retrieve workspace or model: Anaplan service has "
//							+ "not been initialised");
//		}
//		try {
//			final Workspace workspace = openConnection
//					.getWorkspace(workspaceId);
//
//			if (workspace == null) {
//				throw new AnaplanConnectionException(UserMessages.getMessage(
//						"invalidWorkspace", workspaceId));
//			} else {
//				LogUtil.debug(getLogContext(),
//						"successfully retrieved workspace");
//			}
//
//			final Model model = workspace.getModel(modelId);
//			if (model == null) {
//				throw new AnaplanConnectionException(UserMessages.getMessage(
//						"invalidModel", modelId));
//			} else {
//				LogUtil.debug(getLogContext(), "successfully retrieved model");
//				this.model = model;
//			}
//
//		} catch (AnaplanAPIException e) {
//			closeConnection();
//
//			final String msg = "Error retrieving Anaplan model or workspace: "
//					+ e.getMessage();
//			LogUtil.error(getLogContext(), msg, e);
//
//			throw new AnaplanConnectionException(msg, e);
//		}
//
//		LogUtil.debug(getLogContext(),
//				"workspace and model validated successfully");
//	}
}
