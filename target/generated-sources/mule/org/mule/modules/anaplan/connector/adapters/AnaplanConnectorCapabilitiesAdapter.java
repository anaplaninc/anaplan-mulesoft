
package org.mule.modules.anaplan.connector.adapters;

import javax.annotation.Generated;
import org.mule.api.devkit.capability.Capabilities;
import org.mule.api.devkit.capability.ModuleCapability;
import org.mule.modules.anaplan.connector.AnaplanConnector;


/**
 * A <code>AnaplanConnectorCapabilitiesAdapter</code> is a wrapper around {@link AnaplanConnector } that implements {@link org.mule.api.Capabilities} interface.
 * 
 */
@Generated(value = "Mule DevKit Version 3.5.2", date = "2015-01-05T02:40:12-08:00", comments = "Build UNNAMED.2039.0541b23")
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
