package org.mule.modules.anaplan.connector;


//import java.util.Collections;
import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Set;

import org.mule.modules.anaplan.utils.LogUtil;
//import com.boomi.connector.api.PropertyMap;

/**
 * Parameter caching and validation.
 * 
 * Validation supports any property value type, but convenience retrieval
 * methods only added for String as this is all we are currently using.
 */
public class AnaplanConnectorProperties {

//	private static final String REQUIRED_PROPERTY_NOT_FOUND = null;

	private final HashMap<String, String> connectorProperties;

	/**
	 * @param fieldValues
	 * 		      List of values to be used for developing the connector
	 * 			  properties object.	  
	 * @param requiredPropertyFields
	 * 			  Optionally, any fields which are mandatory, and cannot have
	 *            null or empty-string values.
	 */
	public AnaplanConnectorProperties(String[] fieldValues,
			String... requiredPropertyFields) {
		
		connectorProperties = new HashMap<String, String>();
		if (requiredPropertyFields != null) {
			if (fieldValues.length == requiredPropertyFields.length) {
				for (int i = 0; i < requiredPropertyFields.length; i++) {
					connectorProperties.put(requiredPropertyFields[i],
											fieldValues[i]);
				}
			}
		}
		LogUtil.trace(getClass().getSimpleName(), 
				" creating anaplan properties: " + this.toString());
	}

	/**
	 * Checks that all required property fields are present, and have non-null
	 * property values.
	 * 
	 * Optionally throw an exception with useful message if this is not the
	 * case.
	 * 
	 * @param throwExceptionOnFail
	 * @return
	 * @throws IllegalStateException
	 *             if throwExceptionOnFail requested and not all declared
	 *             required properties are present
	 */
	public boolean hasAllRequiredProperties(boolean throwExceptionOnFail) {
		LogUtil.trace(getClass().getSimpleName(), "hasAllRequiredProperties("
				+ throwExceptionOnFail + ")");

//		final Set<String> availableProperties = connectorProperties.keySet();
//		if (!availableProperties.containsAll(requiredPropertyFields)) {
//			final String msg = "Invalid Anaplan Connector configuration: missing required properties: got "
//					+ connectorProperties.keySet()
//					+ ", need "
//					+ requiredPropertyFields;
//			LogUtil.debug(getClass().getSimpleName(),
//					"required props check failed with message: " + msg);
//
//			if (throwExceptionOnFail) {
//				throw new IllegalStateException(msg);
//			} else {
//				return false;
//			}
//		}

		// error message currently displays internal property field id, would be
		// nice to display the UI label instead.
//		for (String propertyField : requiredPropertyFields) {
//			final Object propertyValue = null;  // connectorProperties.get(propertyField);
//			if (propertyValue == null) {
//				final String msg = "Invalid Anaplan Connector configuration: invalid value null for required property "
//						+ propertyField;
//				LogUtil.debug(getClass().getSimpleName(),
//						"required props check failed with message: " + msg);
//
//				if (throwExceptionOnFail) {
//					throw new IllegalStateException(msg);
//				} else {
//					return false;
//				}
//			}
//
////			if (propertyValue instanceof String
////					&& ((String) propertyValue).isEmpty()) {
////				final String msg = "Invalid Anaplan Connector configuration: invalid empty value for required property "
////						+ propertyField;
////				LogUtil.debug(getClass().getSimpleName(),
////						"required props check failed with message: " + msg);
////
////				if (throwExceptionOnFail) {
////					throw new IllegalStateException(msg);
////				} else {
////					return false;
////				}
////			}
//		}

		LogUtil.trace(getClass().getSimpleName(), "hasAllRequiredProperties("
				+ throwExceptionOnFail + ") passed");
		return true;
	}

//	/**
//	 * Retrieve a string property that has been declared as required.
//	 * 
//	 * If {@link #hasAllRequiredProperties(boolean)} passes (and assuming this
//	 * property cache has not been (indirectly) modified since), this method is
//	 * guaranteed to return a non-null result.
//	 * 
//	 * @param propertyField
//	 * @return value of the given property
//	 * @throws IllegalArgumentException
//	 *             if the given property field was not declared as required, or
//	 *             if it is not available.
//	 */
//	public String getRequiredStringProperty(String propertyField) {
//		LogUtil.trace(getClass().getSimpleName(), "getRequiredStringProperty("
//				+ propertyField + ")");
//
//		validateRequiredPropertyField(propertyField);
//		return getStringProperty(propertyField);
//	}

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

//	@Override
//	public String toString() {
//		return "required property fields: " + requiredPropertyFields
//				+ "; all available property fields: "
//				+ connectorProperties.keySet();
//	}

//	private void validateRequiredPropertyField(String propertyField)
//			throws IllegalArgumentException {
//		LogUtil.trace(getClass().getSimpleName(),
//				"validateRequiredPropertyField(" + propertyField + ")");
//
//		if (!requiredPropertyFields.contains(propertyField)) {
//			final String msg = propertyField
//					+ " is not a required property field";
//			LogUtil.debug(getClass().getSimpleName(),
//					"prop validation failed with message: " + msg);
//
//			throw new IllegalArgumentException(msg);
//		}
//
//		if (!connectorProperties.containsKey(propertyField)) {
//			final String msg = propertyField + " is not available";
//			throw new IllegalArgumentException(msg);
//		}
//
//		LogUtil.trace(getClass().getSimpleName(),
//				"validateRequiredPropertyField(" + propertyField + ") passed");
//	}
}
