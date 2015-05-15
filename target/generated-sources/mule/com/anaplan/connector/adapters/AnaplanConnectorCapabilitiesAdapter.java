
package com.anaplan.connector.adapters;

import javax.annotation.Generated;
import com.anaplan.connector.AnaplanConnector;
import org.mule.api.devkit.capability.Capabilities;
import org.mule.api.devkit.capability.ModuleCapability;


/**
 * A <code>AnaplanConnectorCapabilitiesAdapter</code> is a wrapper around {@link AnaplanConnector } that implements {@link org.mule.api.Capabilities} interface.
 * 
 */
@Generated(value = "Mule DevKit Version 3.6.0", date = "2015-04-20T09:44:38-07:00", comments = "Build UNNAMED.2363.ef5c8a7")
public class AnaplanConnectorCapabilitiesAdapter
    extends AnaplanConnector
    implements Capabilities
{


    /**
     * Returns true if this module implements such capability
     * 
     */
    public boolean isCapableOf(ModuleCapability capability) {
        if (capability == ModuleCapability.LIFECYCLE_CAPABLE) {
            return true;
        }
        if (capability == ModuleCapability.CONNECTION_MANAGEMENT_CAPABLE) {
            return true;
        }
        return false;
    }

}
