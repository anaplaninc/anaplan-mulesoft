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

import org.mule.api.annotations.ConnectionStrategy;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.display.FriendlyName;
import org.mule.api.annotations.display.Icons;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Payload;

import com.anaplan.connector.connection.BaseConnectionStrategy;
import com.anaplan.connector.exceptions.AnaplanConnectionException;
import com.anaplan.connector.exceptions.AnaplanOperationException;
import com.anaplan.connector.utils.AnaplanExecuteAction;
import com.anaplan.connector.utils.AnaplanExportOperation;
import com.anaplan.connector.utils.AnaplanImportOperation;


/**
 * Anaplan Connector that supports Anaplan actions such as Import, Export,
 * and Delete.
 *
 * @author MuleSoft, Inc.
 * @author Spondon Saha.
 */
@Icons(connectorLarge="../../../icons/anaplan-connector-48x32-logo.png",
	   connectorSmall="../../../icons/anaplan-connector-16x16.png")
@Connector(name="anaplan", schemaVersion="1.0", friendlyName="Anaplan")
public class AnaplanConnector {

	private static AnaplanExportOperation exporter;
	private static AnaplanImportOperation importer;
	private static AnaplanExecuteAction runner;

	@ConnectionStrategy
	private BaseConnectionStrategy connectionStrategy;

	/**
	 * Getter for connectionStrategy.
	 * @return
	 */
	public BaseConnectionStrategy getConnectionStrategy() {
        return this.connectionStrategy;
    }

	/**
	 * Setter for connectionStrategy
	 * @param connStrategy
	 */
    public void setConnectionStrategy(BaseConnectionStrategy connStrategy) {
        this.connectionStrategy = connStrategy;
    }

	/**
	 * Reads in CSV data that represents an Anaplan model, delimited by the
	 * provided delimiter, parses it, then loads it into an Anaplan model.
	 *
	 * @param data
	 * @param anaplanWorkspaceId
	 * @param anaplanModelId
	 * @param anaplanImportId
	 * @param delimiter
	 * @throws AnaplanConnectionException
	 * @throws AnaplanOperationException
	 */
	@Processor(friendlyName = "Import")
	public void importToModel(
			@Payload String data,
			@FriendlyName("Workspace name or ID") String workspaceId,
		    @FriendlyName("Model name or ID") String modelId,
		    @FriendlyName("Import name or ID") String importId,
			@FriendlyName("Column separator") @Default(",") String columnSeparator,
			@FriendlyName("Delimiter") @Default("\"") String delimiter)
		    		throws AnaplanConnectionException,
		    			   AnaplanOperationException {
		// validate API connectionStrategy
		connectionStrategy.validateConnection();

		// start the import
		importer = new AnaplanImportOperation(connectionStrategy.getService());
		importer.runImport(data, workspaceId, modelId, importId,
				columnSeparator, delimiter);
	}

	/**
	 * Run an export of an Anaplan Model specified by workspace-ID, model-ID and
	 * the export-ID. At the end of each export, the connectionStrategy is dropped,
	 * hence a check needs to be made to verify if the current connectionStrategy
	 * exists. If not, re-establish it by calling .openConnection().
	 *
	 * @return CSV string.
	 * @throws AnaplanConnectionException
	 */
	@Processor(friendlyName = "Export")
	public String exportFromModel(
			@FriendlyName("Workspace name or ID") String workspaceId,
			@FriendlyName("Model name or ID") String modelId,
			@FriendlyName("Export name or ID") String exportId)
					throws AnaplanConnectionException,
						   AnaplanOperationException {
		// validate API connectionStrategy
		connectionStrategy.validateConnection();

		// start the export
		exporter = new AnaplanExportOperation(connectionStrategy.getService());
		return exporter.runExport(workspaceId, modelId, exportId);
	}

	/**
	 * Deletes data from a model by executing the respective delete action.
	 *
	 * @throws AnaplanConnectionException
	 * @throws AnaplanOperationException
	 */
	@Processor(friendlyName = "Execute Action")
	public void executeAction(
			@FriendlyName("Workspace name or ID") String workspaceId,
			@FriendlyName("Model name or ID") String modelId,
			@FriendlyName("Import name or ID") String actionId)
					throws AnaplanConnectionException,
						   AnaplanOperationException {
		// validate the API connectionStrategy
		connectionStrategy.validateConnection();

		// start the delete process
		runner = new AnaplanExecuteAction(connectionStrategy.getService());
		runner.runExecute(workspaceId, modelId, actionId);
	}
}
