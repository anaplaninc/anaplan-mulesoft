
package com.anaplan.connector.connectivity;

import javax.annotation.Generated;
import com.anaplan.connector.connection.BasicAuthConnectionStrategy;
import org.mule.api.ConnectionException;
import org.mule.devkit.shade.connection.management.ConnectionManagementConnectionAdapter;

@Generated(value = "Mule DevKit Version 3.6.0", date = "2015-04-20T09:44:38-07:00", comments = "Build UNNAMED.2363.ef5c8a7")
public class BasicAuthConnectionStrategyAnaplanConnectorAdapter
    extends BasicAuthConnectionStrategy
    implements ConnectionManagementConnectionAdapter<BasicAuthConnectionStrategy, ConnectionManagementBasicAuthConnectionAnaplanConnectorConnectionKey>
{


    @Override
    public void connect(ConnectionManagementBasicAuthConnectionAnaplanConnectorConnectionKey connectionKey)
        throws ConnectionException
    {
        super.connect(connectionKey.getUsername(), connectionKey.getPassword(), connectionKey.getUrl(), connectionKey.getProxyHost(), connectionKey.getProxyUser(), connectionKey.getProxyPass());
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
    public BasicAuthConnectionStrategy getStrategy() {
        return this;
    }

}
