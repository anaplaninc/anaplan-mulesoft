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

	public AnaplanConnectorProperties() {
		connectorProperties = new HashMap<String, String>();
	}

	/**
	 * @param fieldValues
	 * 		      List of values to be used for developing the connector
	 * 			  properties object.
	 * @param requiredPropertyFields
	 * 			  Optionally, any fields which are mandatory, and cannot have
	 *            null or empty-string values.
	 */
	public void setProperties(String[] fieldValues,
							  String... requiredPropertyFields)
									  throws ConnectorPropertiesException {
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
	 * @param propertyField, Property key name stored in this object.
	 * @return value of the given property, or
	 *         {@value #REQUIRED_PROPERTY_NOT_FOUND}
	 */
	public String getStringProperty(String propertyField) {
		LogUtil.trace(getClass().getSimpleName(), "getStringProperty("
				+ propertyField + ")");
		return connectorProperties.get(propertyField);
	}
}
