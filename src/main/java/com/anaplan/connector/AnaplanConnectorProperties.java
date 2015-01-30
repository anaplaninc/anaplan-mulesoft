/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package com.anaplan.connector;


import java.util.HashMap;

import com.anaplan.connector.exceptions.ConnectorPropertiesException;
import com.anaplan.connector.utils.LogUtil;


/**
 * Parameter caching and validation.
 *
 * Validation supports any property value type, but convenience retrieval
 * methods only added for String as this is all we are currently using.
 */
public class AnaplanConnectorProperties {

	private final HashMap<String, String> connectorProperties;

	/**
	 * @param fieldValues
	 * 		      List of values to be used for developing the connector
	 * 			  properties object.
	 * @param requiredPropertyFields
	 * 			  Optionally, any fields which are mandatory, and cannot have
	 *            null or empty-string values.
	 */
	public AnaplanConnectorProperties() {
		connectorProperties = new HashMap<String, String>();
	}

	public void setProperties(String[] fieldValues,
			String... requiredPropertyFields) throws ConnectorPropertiesException {
		if (requiredPropertyFields != null) {
			if (fieldValues.length == requiredPropertyFields.length) {
				for (int i = 0; i < requiredPropertyFields.length; i++) {
					connectorProperties.put(requiredPropertyFields[i],
											fieldValues[i]);
				}
			} else {
				throw new ConnectorPropertiesException("Provided field-values "
						+ "and required property-fields are of different "
						+ "lengths!!");
			}
		}
		LogUtil.trace(getClass().getSimpleName(),
				" creating anaplan properties: " + this.toString());
	}

	/**
	 * Retrieve any required or optional property.
	 *
	 * @param propertyField
	 * @return value of the given property, or
	 *         {@value #REQUIRED_PROPERTY_NOT_FOUND}
	 */
	public String getStringProperty(String propertyField) {
		LogUtil.trace(getClass().getSimpleName(), "getStringProperty("
				+ propertyField + ")");
		return connectorProperties.get(propertyField);
	}
}
