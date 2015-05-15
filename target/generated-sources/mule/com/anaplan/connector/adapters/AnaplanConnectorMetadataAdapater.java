
package com.anaplan.connector.adapters;

import javax.annotation.Generated;
import com.anaplan.connector.AnaplanConnector;
import org.mule.api.MetadataAware;


/**
 * A <code>AnaplanConnectorMetadataAdapater</code> is a wrapper around {@link AnaplanConnector } that adds support for querying metadata about the extension.
 * 
 */
@Generated(value = "Mule DevKit Version 3.6.0", date = "2015-04-20T09:44:38-07:00", comments = "Build UNNAMED.2363.ef5c8a7")
public class AnaplanConnectorMetadataAdapater
    extends AnaplanConnectorCapabilitiesAdapter
    implements MetadataAware
{

    private final static String MODULE_NAME = "Anaplan";
    private final static String MODULE_VERSION = "1.0.0";
    private final static String DEVKIT_VERSION = "3.6.0";
    private final static String DEVKIT_BUILD = "UNNAMED.2363.ef5c8a7";
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
