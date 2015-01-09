
package org.mule.modules.anaplan.connector.adapters;

import javax.annotation.Generated;
import org.mule.api.MetadataAware;
import org.mule.modules.anaplan.connector.AnaplanConnector;


/**
 * A <code>AnaplanConnectorMetadataAdapater</code> is a wrapper around {@link AnaplanConnector } that adds support for querying metadata about the extension.
 * 
 */
@Generated(value = "Mule DevKit Version 3.5.2", date = "2015-01-08T02:50:03-08:00", comments = "Build UNNAMED.2039.0541b23")
public class AnaplanConnectorMetadataAdapater
    extends AnaplanConnectorCapabilitiesAdapter
    implements MetadataAware
{

    private final static String MODULE_NAME = "Anaplan";
    private final static String MODULE_VERSION = "1.0.0-SNAPSHOT";
    private final static String DEVKIT_VERSION = "3.5.2";
    private final static String DEVKIT_BUILD = "UNNAMED.2039.0541b23";
    private final static String MIN_MULE_VERSION = "3.5.0";

    public String getModuleName() {
        return MODULE_NAME;
    }

    public String getModuleVersion() {
        return MODULE_VERSION;
    }

    public String getDevkitVersion() {
        return DEVKIT_VERSION;
    }

    public String getDevkitBuild() {
        return DEVKIT_BUILD;
    }

    public String getMinMuleVersion() {
        return MIN_MULE_VERSION;
    }

}
