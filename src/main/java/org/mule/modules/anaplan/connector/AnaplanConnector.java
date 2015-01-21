/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package org.mule.modules.anaplan.connector;

import java.util.ArrayList;
import java.util.List;

import org.mule.api.ConnectionException;
import org.mule.api.ConnectionExceptionCode;
import org.mule.api.annotations.Connect;
import org.mule.api.annotations.ConnectionIdentifier;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Disconnect;
import org.mule.api.annotations.MetaDataKeyRetriever;
import org.mule.api.annotations.MetaDataRetriever;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.ValidateConnection;
import org.mule.api.annotations.display.Password;
import org.mule.api.annotations.param.ConnectionKey;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.common.metadata.DefaultMetaData;
import org.mule.common.metadata.DefaultMetaDataKey;
import org.mule.common.metadata.MetaData;
import org.mule.common.metadata.MetaDataKey;
import org.mule.common.metadata.MetaDataModel;
import org.mule.common.metadata.builder.DefaultMetaDataBuilder;
import org.mule.common.metadata.builder.DynamicObjectBuilder;
import org.mule.common.metadata.datatype.DataType;
import org.mule.modules.anaplan.connector.exceptions.AnaplanConnectionException;
import org.mule.modules.anaplan.connector.exceptions.AnaplanExportOperationException;
import org.mule.modules.anaplan.connector.utils.AnaplanExportOperation;
import org.mule.modules.anaplan.connector.utils.LogUtil;

import com.anaplan.client.Service;

/**
 * Anaplan Connector built using Anypoint Studio to export, import, update
 * and delete Anaplan models.
 *
 * @author MuleSoft, Inc.
 * @author Spondon Saha.
 */
@Connector(name = "anaplan", schemaVersion = "1.0", friendlyName = "Anaplan")
public class AnaplanConnector {

	/**
	 * Stores the connection to the Anaplan API
	 */
	private AnaplanConnection apiConn;

	private static AnaplanExportOperation exporter;

//	private Service service;

	/**
	 * Retrieves the list of keys
	 */
	@MetaDataKeyRetriever
	public List<MetaDataKey> getMetaDataKeys() throws Exception {
		List<MetaDataKey> keys = new ArrayList<MetaDataKey>();

		// Generate the keys
		keys.add(new DefaultMetaDataKey("id1", "User"));
		keys.add(new DefaultMetaDataKey("id2", "Book"));

		return keys;
	}

	/**
	 * Get MetaData given the Key the user selects
	 *
	 * @param key
	 *            The key selected from the list of valid keys
	 * @return The MetaData model of that corresponds to the key
	 * @throws Exception
	 *             If anything fails
	 */
	@MetaDataRetriever
	public MetaData getMetaData(MetaDataKey key) throws Exception {
		DefaultMetaDataBuilder builder = new DefaultMetaDataBuilder();
		// If you have a Pojo class
		// PojoMetaDataBuilder<?> pojoObject=builder.createPojo(Pojo.class);

		// If you use maps as input of your processors that work with DataSense
		DynamicObjectBuilder<?> dynamicObject = builder.createDynamicObject(key
				.getId());

		if (key.getId().equals("id1")) {
			dynamicObject.addSimpleField("Username", DataType.STRING);
			dynamicObject.addSimpleField("age", DataType.INTEGER);
		} else {
			dynamicObject.addSimpleField("Author", DataType.STRING);
			dynamicObject.addSimpleField("Tittle", DataType.STRING);
		}
		MetaDataModel model = builder.build();
		MetaData metaData = new DefaultMetaData(model);

		return metaData;
	}

	/**
	 * Create a record
	 *
	 * @return
	 */
	@Processor(friendlyName = "Import")
	public boolean importModel() {
		return false;
	}

	/**
	 * Run an export of an Anaplan Model specified by workspace-ID, model-ID and
	 * the export-ID. At the end of each export, the connection is dropped,
	 * hence a check needs to be made to verify if the current connection
	 * exists. If not, re-establish it by calling .openConnection().
	 *
	 * @return Serializable response object.
	 * @throws AnaplanConnectionException
	 */
	@Processor(friendlyName = "Export")
	public String exportModel(String anaplanWorkspaceId, String anaplanModelId,
			String anaplanExportIdOrName) throws AnaplanConnectionException {
		String data = null;

		// validate API connection
		if (apiConn.getConnection() == null) {
			try {
				apiConn.openConnection();
			} catch (AnaplanConnectionException e) {
				e.printStackTrace();
				throw e;
			}
		} else {
			LogUtil.status(apiConn.getLogContext(),
					"Connection to API exists. Proceeding with export...");
		}
		// start the export
		exporter = new AnaplanExportOperation(apiConn);
		try {
			data = exporter.runExport(anaplanWorkspaceId, anaplanModelId,
					anaplanExportIdOrName);
		} catch (AnaplanExportOperationException e) {
			LogUtil.error(apiConn.getLogContext(), e.getMessage());
		}
		return data;
	}

	/**
	 * Updates a record
	 *
	 * @return
	 */
	@Processor(friendlyName = "Update")
	public boolean updateModel(String anaplanModelId) {
		return false;
	}

	/**
	 * Deletes a record
	 *
	 * @return
	 */
	@Processor(friendlyName = "Delete")
	public boolean deleteModel(String anaplanModelId) {
		return false;
	}

	/**
	 * Connect to the Anaplan API.
	 *
	 * @param username
	 * @param password
	 * @throws ConnectionException
	 */
	@Connect
	public synchronized void connect(
			@ConnectionKey String username,
			@Password String password,
			@Default("https://api.anaplan.com") String url,
			@Optional @Default("") String proxyHost,
			@Optional @Default("") String proxyUser,
			@Optional @Default("") String proxyPass)
					throws org.mule.api.ConnectionException {

		LogUtil.status(getClass().toString(), "Initiating connection...");
		Service service = null;

		if (apiConn == null) {
			apiConn = new AnaplanConnection(username, password, url, proxyHost,
					proxyUser, proxyPass);
			try {
				service = apiConn.openConnection();
			} catch (AnaplanConnectionException e) {
				throw new org.mule.api.ConnectionException(
						ConnectionExceptionCode.INCORRECT_CREDENTIALS, null,
						e.getMessage(), e);
			}

			if (service == null) {
				throw new org.mule.api.ConnectionException(
						ConnectionExceptionCode.UNKNOWN, null, "No service "
						+ "object acquired after opening connection to Anaplan "
						+ "API!", null);
			} else {
				LogUtil.status(getClass().toString(),
						"Successfully connected to Anaplan API!");
			}
		}
	}

	/**
	 * Disconnect
	 */
	@Disconnect
	public void disconnect() {
		if (apiConn != null) {
			apiConn.closeConnection();
		} else {
			LogUtil.error(getClass().toString(), "No connection to disconnect!");
		}
	}

	/**
	 * Are we connected?
	 */
	@ValidateConnection
	public boolean isConnected() {
		if (apiConn != null) {
			return true;
		}
		return false;
	}

	/**
	 * Are we connected?
	 */
	@ConnectionIdentifier
	public String connectionId() {
		if (apiConn != null)
			return apiConn.getConnectionId();
		else
			return "Not connected!";
	}

	/**
	 * Custom processor
	 *
	 * {@sample.xml ../../../doc/anaplan-connector.xml.sample
	 * anaplan:my-processor}
	 *
	 * @param content
	 *            Content to be processed
	 * @return Some string
	 */
//	@Processor
//	public String myProcessor(String content) {
//		/*
//		 * MESSAGE PROCESSOR CODE GOES HERE
//		 */
//		return content;
//	}
}
