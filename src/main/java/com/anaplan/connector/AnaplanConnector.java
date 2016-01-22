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

import com.anaplan.connector.utils.*;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.ConnectionStrategy;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.display.FriendlyName;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Payload;

import com.anaplan.connector.connection.BaseConnectionStrategy;
import com.anaplan.connector.exceptions.AnaplanConnectionException;
import com.anaplan.connector.exceptions.AnaplanOperationException;


/**
 * Anaplan Connector that supports Anaplan actions such as Import, Export,
 * and Delete.
 *
 * @author MuleSoft, Inc.
 * @author Spondon Saha.
 */
@Connector(name="anaplan", schemaVersion="3.6", friendlyName="Anaplan",
           minMuleVersion="3.6")
public class AnaplanConnector {

	private static AnaplanExportOperation exporter;
	private static AnaplanImportOperation importer;
	private static AnaplanDeleteOperation deleter;
	private static AnaplanProcessOperation processRunner;

	@Config
	private BaseConnectionStrategy connectionStrategy;

	/**
	 * Getter for connectionStrategy.
	 * @return Certificate or Basic connection strategy.
	 */
	public BaseConnectionStrategy getConnectionStrategy() {
        return this.connectionStrategy;
    }

	/**
	 * Setter for connectionStrategy
	 * @param connStrategy Basic or Certificate connection strategy.
	 */
    public void setConnectionStrategy(BaseConnectionStrategy connStrategy) {
        this.connectionStrategy = connStrategy;
    }

    /**
     * Reads in CSV data that represents an Anaplan model, delimited by the
	 * provided delimiter, parses it, then loads it into an Anaplan model.
	 *
	 * {@sample.xml ../../../doc/anaplan-connector.xml.sample anaplan:import-to-model}
     *
     * @param data Stringified CSV data that is to be imported into Anaplan.
     * @param workspaceId Anaplan workspace ID.
     * @param modelId Anaplan model ID.
     * @param importId Action ID of the Import operation.
     * @param columnSeparator Column separator, defaults to comma.
     * @param delimiter Cell escape values, defaults to double-quotes.
     * @return Status message from running the Import operation.
     * @throws AnaplanConnectionException When an error occurs during
     * 									  authentication
     * @throws AnaplanOperationException When the Import operation encounters an
     * 									 error.
     */
	@Processor(friendlyName = "Import")
	public String importToModel(
			@Payload String data,
			@FriendlyName("Workspace name or ID") String workspaceId,
		    @FriendlyName("Model name or ID") String modelId,
		    @FriendlyName("Import name or ID") String importId,
			@FriendlyName("Column separator")
			@Default(Delimiters.COMMA) String columnSeparator,
			@FriendlyName("Delimiter")
			@Default(Delimiters.ESCAPE_CHARACTER) String delimiter)
		    		throws AnaplanConnectionException,
		    			   AnaplanOperationException {
		// validate API connectionStrategy
		connectionStrategy.validateConnection();

		// start the import
		importer = new AnaplanImportOperation(
				connectionStrategy.getApiConnection());
		return importer.runImport(data, workspaceId, modelId, importId,
				columnSeparator, delimiter);
	}

	/**
	 * Run an export of an Anaplan Model specified by workspace-ID, model-ID and
	 * the export-ID. At the end of each export, the connectionStrategy is dropped,
	 * hence a check needs to be made to verify if the current connectionStrategy
	 * exists. If not, re-establish it by calling .openConnection().
	 *
	 * {@sample.xml ../../../doc/anaplan-connector.xml.sample anaplan:export-from-model}
	 *
	 * @param workspaceId Anaplan workspace ID.
	 * @param modelId Anaplan model ID.
	 * @param exportId Action ID of the export operation.
	 * @return CSV string.
	 * @throws AnaplanConnectionException When an error occurs at authentication.
	 * @throws AnaplanOperationException When the Export operation encounters an
	 * 									 error.
	 */
	@Processor(friendlyName="Export")
	public String exportFromModel(
			@FriendlyName("Workspace name or ID") String workspaceId,
			@FriendlyName("Model name or ID") String modelId,
			@FriendlyName("Export name or ID") String exportId)
					throws AnaplanConnectionException,
						   AnaplanOperationException {
		// validate API connectionStrategy
		connectionStrategy.validateConnection();

		// start the export
		exporter = new AnaplanExportOperation(
				connectionStrategy.getApiConnection());
		return exporter.runExport(workspaceId, modelId, exportId);
	}

	/**
	 * Deletes data from a model by executing the respective delete action.
	 *
	 * {@sample.xml ../../../doc/anaplan-connector.xml.sample anaplan:delete-from-model}
	 *
	 * @param workspaceId Anaplan workspace ID.
	 * @param modelId Anaplan model ID.
	 * @param deleteActionId Anaplan delete action ID.
	 * @return Status Any response message from running the Delete Action.
	 * @throws AnaplanConnectionException When an error occurs at authentication.
	 * @throws AnaplanOperationException When the Action encounters an error
	 * 									 while executing.
	 */
	@Processor(friendlyName="Delete")
	public String deleteFromModel(
			@FriendlyName("Workspace name or ID") String workspaceId,
			@FriendlyName("Model name or ID") String modelId,
			@FriendlyName("Delete action name or ID") String deleteActionId)
					throws AnaplanConnectionException,
						   AnaplanOperationException {
		// validate the API connectionStrategy
		connectionStrategy.validateConnection();

		// start the delete process
		deleter = new AnaplanDeleteOperation(
				connectionStrategy.getApiConnection());
		return deleter.runDeleteAction(workspaceId, modelId, deleteActionId);
	}

	/**
	 * Runs a specific Anaplan Process, which could be a multitude of actions
	 * represented as a single Anaplan process.
	 *
	 * {@sample.xml ../../../doc/anaplan-connector.xml.sample anaplan:run-process}
	 *
	 * @param workspaceId Anaplan workspace ID against which to run the process.
	 * @param modelId Anaplan model ID against which to run the process.
	 * @param processId Anaplan process ID
	 * @return Status response string of running the process, along with any
	 * 		failure dump strings as applicable.
	 * @throws AnaplanConnectionException Thrown when if API connection errors out.
	 * @throws AnaplanOperationException Thrown when the Process operation fails.
	 */
	@Processor(friendlyName="Process")
	public String runProcess(
			@FriendlyName("Workspace name or ID") String workspaceId,
			@FriendlyName("Model name or ID") String modelId,
			@FriendlyName("Process name or ID") String processId)
					throws AnaplanConnectionException,
						   AnaplanOperationException {
		// validate the API connectionStrategy
		connectionStrategy.validateConnection();

		// run the process
		processRunner = new AnaplanProcessOperation(
				connectionStrategy.getApiConnection());
		return processRunner.runProcess(workspaceId, modelId, processId);

	}
}
