
package com.anaplan.connector.connectivity;

import javax.annotation.Generated;
import com.anaplan.connector.connection.CertAuthConnectionStrategy;
import org.mule.api.ConnectionException;
import org.mule.devkit.shade.connection.management.ConnectionManagementConnectionAdapter;

@Generated(value = "Mule DevKit Version 3.6.0", date = "2015-04-20T09:44:38-07:00", comments = "Build UNNAMED.2363.ef5c8a7")
public class CertAuthConnectionStrategyAnaplanConnectorAdapter
    extends CertAuthConnectionStrategy
    implements ConnectionManagementConnectionAdapter<CertAuthConnectionStrategy, ConnectionManagementCertAuthConnectionAnaplanConnectorConnectionKey>
{


    @Override
    public void connect(ConnectionManagementCertAuthConnectionAnaplanConnectorConnectionKey connectionKey)
        throws ConnectionException
    {
        super.connect(connectionKey.getCertificatePath(), connectionKey.getUrl(), connectionKey.getProxyHost(), connectionKey.getProxyUser(), connectionKey.getProxyPass());
    }

    @Override
    public void disconnect() {
        super.disconnect();
    }

    @Override
    public String connectionId() {
        return super.connectionId();
    }

    @Override
    public boolean isConnected() {
        return super.isConnected();
    }

    @Override
    public CertAuthConnectionStrategy getStrategy() {
        return this;
    }

}
