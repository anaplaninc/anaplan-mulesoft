/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package com.anaplan.connector.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralise handling of messages displayed to users.
 * 
 * Any message may have an arbitrary (runtime) detail suffix when displayed.
 */
public class UserMessages {
	private static Map<String, String> messages = new HashMap<String, String>();

	private static void addMessage(String messageKey, String message) {
		if (messageKey == null) {
			throw new IllegalArgumentException("message key cannot be null");
		}
		if (message == null) {
			throw new IllegalArgumentException("message body cannot be null");
		}
		messages.put(messageKey, message);
	}

	public static String getMessage(String messageKey) {
		if (messageKey == null) {
			throw new IllegalArgumentException("message key cannot be null");
		}

		final String messageBody = messages.get(messageKey);
		if (messageBody == null) {
			throw new IllegalArgumentException("no message found for key "
					+ messageKey);
		}

		return messageBody;
	}

	public static String getMessage(String messageKey, String detail) {
		final String message = getMessage(messageKey);
		return message + ": " + detail;
	}

	static {
		addMessage("modelSuccess", "Successfully connected to Anaplan Model");
		addMessage("importSuccess", "Import completed successfully");
		addMessage("noInput",
				"Cannot import to Anaplan: no input data provided");
		addMessage("streamUnsupported",
				"Cannot import multiple documents to Anaplan.");
		addMessage("invalidModel", "Invalid model Id");
		addMessage("invalidWorkspace", "Invalid workspace Id");
		addMessage("invalidImport", "Invalid import Id");
		addMessage("invalidExport", "Invalid export Id");
		addMessage("invalidFile", "Invalid file Id");
		addMessage("invalidApiUri",
				"Error: Invalid URI syntax in specified endpoint");
		addMessage("exportStartWrite",
				"Export processing complete, starting write");
		addMessage("exportRetrieveError",
				"Could not retrieve processed export data");
		addMessage("missingFile",
				"Nothing to write: data file could not be retrieved");
		addMessage("accessFail",
				"Wrong username or password, or no workspaces accessible");
		addMessage("apiConnectFail", "Error accessing Anaplan API");
		addMessage("workspaceAccessFail", "Error accessing Anaplan Workspace");
		addMessage("modelAccessFail", "Error accessing Anaplan Model");
		addMessage("workspaceOrModelAccessFail",
				"Error accessing Anaplan Workspace or Model");
		addMessage("failureDump", "Failed records report available");
		addMessage("noFailureDump", "No failed records report available");
		addMessage("importBadData",
				"Some records were not imported: check connector output data for details");
		addMessage("executeActionSuccess", "Successfully executed delete Action");
	}
}
