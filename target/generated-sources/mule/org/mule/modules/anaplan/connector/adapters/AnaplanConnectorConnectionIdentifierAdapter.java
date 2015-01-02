
package org.mule.modules.anaplan.connector.adapters;

import javax.annotation.Generated;
import org.mule.modules.anaplan.connector.AnaplanConnector;
import org.mule.modules.anaplan.connector.connection.Connection;


/**
 * A <code>AnaplanConnectorConnectionIdentifierAdapter</code> is a wrapper around {@link AnaplanConnector } that implements {@link org.mule.devkit.dynamic.api.helper.Connection} interface.
 * 
 */
@Generated(value = "Mule DevKit Version 3.5.2", date = "2015-01-02T02:35:36-08:00", comments = "Build UNNAMED.2039.0541b23")
public class AnaplanConnectorConnectionIdentifierAdapter
    extends AnaplanConnectorProcessAdapter
    implements Connection
{


    public String getConnectionIdentifier() {
        return super.connectionId();
    }

}
