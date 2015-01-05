
package org.mule.modules.anaplan.connector.connectivity;

import javax.annotation.Generated;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.Startable;
import org.mule.api.lifecycle.Stoppable;
import org.mule.modules.anaplan.connector.adapters.AnaplanConnectorConnectionIdentifierAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Generated(value = "Mule DevKit Version 3.5.2", date = "2015-01-05T02:40:12-08:00", comments = "Build UNNAMED.2039.0541b23")
public class AnaplanConnectorConnectionFactory implements KeyedPoolableObjectFactory
{

    private static Logger logger = LoggerFactory.getLogger(AnaplanConnectorConnectionFactory.class);
    private AnaplanConnectorConnectionManager connectionManager;

    public AnaplanConnectorConnectionFactory(AnaplanConnectorConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public Object makeObject(Object key)
        throws Exception
    {
        if (!(key instanceof AnaplanConnectorConnectionKey)) {
            if (key == null) {
                logger.warn("Connection key is null");
            } else {
                logger.warn("Cannot cast key of type ".concat(key.getClass().getName().concat(" to ").concat("org.mule.modules.anaplan.connector.connectivity.AnaplanConnectorConnectionKey")));
            }
            throw new RuntimeException("Invalid key type ".concat(key.getClass().getName()));
        }
        AnaplanConnectorConnectionIdentifierAdapter connector = new AnaplanConnectorConnectionIdentifierAdapter();
        if (connector instanceof MuleContextAware) {
            ((MuleContextAware) connector).setMuleContext(connectionManager.getMuleContext());
        }
        if (connector instanceof Initialisable) {
            ((Initialisable) connector).initialise();
        }
        if (connector instanceof Startable) {
            ((Startable) connector).start();
        }
        if (!connector.isConnected()) {
            connector.connect(((AnaplanConnectorConnectionKey) key).getUsername(), ((AnaplanConnectorConnectionKey) key).getPassword(), ((AnaplanConnectorConnectionKey) key).getUrl(), ((AnaplanConnectorConnectionKey) key).getWorkspaceId(), ((AnaplanConnectorConnectionKey) key).getModelId(), ((AnaplanConnectorConnectionKey) key).getProxyHost(), ((AnaplanConnectorConnectionKey) key).getProxyUser(), ((AnaplanConnectorConnectionKey) key).getProxyPass());
        }
        return connector;
    }

    public void destroyObject(Object key, Object obj)
        throws Exception
    {
        if (!(key instanceof AnaplanConnectorConnectionKey)) {
            if (key == null) {
                logger.warn("Connection key is null");
            } else {
                logger.warn("Cannot cast key of type ".concat(key.getClass().getName().concat(" to ").concat("org.mule.modules.anaplan.connector.connectivity.AnaplanConnectorConnectionKey")));
            }
            throw new RuntimeException("Invalid key type ".concat(key.getClass().getName()));
        }
        if (!(obj instanceof AnaplanConnectorConnectionIdentifierAdapter)) {
            if (obj == null) {
                logger.warn("Connector is null");
            } else {
                logger.warn("Cannot cast connector of type ".concat(obj.getClass().getName().concat(" to ").concat("org.mule.modules.anaplan.connector.adapters.AnaplanConnectorConnectionIdentifierAdapter")));
            }
            throw new RuntimeException("Invalid connector type ".concat(obj.getClass().getName()));
        }
        try {
            ((AnaplanConnectorConnectionIdentifierAdapter) obj).disconnect();
        } catch (Exception e) {
            throw e;
        } finally {
            if (((AnaplanConnectorConnectionIdentifierAdapter) obj) instanceof Stoppable) {
                ((Stoppable) obj).stop();
            }
            if (((AnaplanConnectorConnectionIdentifierAdapter) obj) instanceof Disposable) {
                ((Disposable) obj).dispose();
            }
        }
    }

    public boolean validateObject(Object key, Object obj) {
        if (!(obj instanceof AnaplanConnectorConnectionIdentifierAdapter)) {
            if (obj == null) {
                logger.warn("Connector is null");
            } else {
                logger.warn("Cannot cast connector of type ".concat(obj.getClass().getName().concat(" to ").concat("org.mule.modules.anaplan.connector.adapters.AnaplanConnectorConnectionIdentifierAdapter")));
            }
            throw new RuntimeException("Invalid connector type ".concat(obj.getClass().getName()));
        }
        try {
            return ((AnaplanConnectorConnectionIdentifierAdapter) obj).isConnected();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public void activateObject(Object key, Object obj)
        throws Exception
    {
        if (!(key instanceof AnaplanConnectorConnectionKey)) {
            throw new RuntimeException("Invalid key type");
        }
        if (!(obj instanceof AnaplanConnectorConnectionIdentifierAdapter)) {
            throw new RuntimeException("Invalid connector type");
        }
        try {
            if (!((AnaplanConnectorConnectionIdentifierAdapter) obj).isConnected()) {
                ((AnaplanConnectorConnectionIdentifierAdapter) obj).connect(((AnaplanConnectorConnectionKey) key).getUsername(), ((AnaplanConnectorConnectionKey) key).getPassword(), ((AnaplanConnectorConnectionKey) key).getUrl(), ((AnaplanConnectorConnectionKey) key).getWorkspaceId(), ((AnaplanConnectorConnectionKey) key).getModelId(), ((AnaplanConnectorConnectionKey) key).getProxyHost(), ((AnaplanConnectorConnectionKey) key).getProxyUser(), ((AnaplanConnectorConnectionKey) key).getProxyPass());
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void passivateObject(Object key, Object obj)
        throws Exception
    {
    }

}
