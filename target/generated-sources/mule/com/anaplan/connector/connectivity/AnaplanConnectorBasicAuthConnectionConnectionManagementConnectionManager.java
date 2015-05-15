
package com.anaplan.connector.connectivity;

import javax.annotation.Generated;
import com.anaplan.connector.AnaplanConnector;
import com.anaplan.connector.adapters.AnaplanConnectorConnectionManagementAdapter;
import com.anaplan.connector.connection.BasicAuthConnectionStrategy;
import com.anaplan.connector.pooling.DevkitGenericKeyedObjectPool;
import org.apache.commons.pool.KeyedObjectPool;
import org.mule.api.MetadataAware;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.config.MuleProperties;
import org.mule.api.context.MuleContextAware;
import org.mule.api.devkit.ProcessAdapter;
import org.mule.api.devkit.ProcessTemplate;
import org.mule.api.devkit.capability.Capabilities;
import org.mule.api.devkit.capability.ModuleCapability;
import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.retry.RetryPolicyTemplate;
import org.mule.common.DefaultTestResult;
import org.mule.common.TestResult;
import org.mule.common.Testable;
import org.mule.config.PoolingProfile;
import org.mule.devkit.processor.ExpressionEvaluatorSupport;
import org.mule.devkit.shade.connection.management.ConnectionManagementConnectionAdapter;
import org.mule.devkit.shade.connection.management.ConnectionManagementConnectionManager;
import org.mule.devkit.shade.connection.management.ConnectionManagementConnectorAdapter;
import org.mule.devkit.shade.connection.management.ConnectionManagementConnectorFactory;
import org.mule.devkit.shade.connection.management.ConnectionManagementProcessTemplate;
import org.mule.devkit.shade.connection.management.UnableToAcquireConnectionException;
import org.mule.devkit.shade.connectivity.ConnectivityTestingErrorHandler;


/**
 * A {@code AnaplanConnectorBasicAuthConnectionConnectionManagementConnectionManager} is a wrapper around {@link AnaplanConnector } that adds connection management capabilities to the pojo.
 * 
 */
@Generated(value = "Mule DevKit Version 3.6.0", date = "2015-04-20T09:44:38-07:00", comments = "Build UNNAMED.2363.ef5c8a7")
public class AnaplanConnectorBasicAuthConnectionConnectionManagementConnectionManager
    extends ExpressionEvaluatorSupport
    implements MetadataAware, MuleContextAware, ProcessAdapter<AnaplanConnectorConnectionManagementAdapter> , Capabilities, Disposable, Initialisable, Testable, ConnectionManagementConnectionManager<ConnectionManagementBasicAuthConnectionAnaplanConnectorConnectionKey, AnaplanConnectorConnectionManagementAdapter, BasicAuthConnectionStrategy>
{

    /**
     * 
     */
    private String username;
    /**
     * 
     */
    private String password;
    /**
     * 
     */
    private String url;
    /**
     * 
     */
    private String proxyHost;
    /**
     * 
     */
    private String proxyUser;
    /**
     * 
     */
    private String proxyPass;
    /**
     * Mule Context
     * 
     */
    protected MuleContext muleContext;
    /**
     * Connector Pool
     * 
     */
    private KeyedObjectPool connectionPool;
    protected PoolingProfile poolingProfile;
    protected RetryPolicyTemplate retryPolicyTemplate;
    private final static String MODULE_NAME = "Anaplan";
    private final static String MODULE_VERSION = "1.0.0";
    private final static String DEVKIT_VERSION = "3.6.0";
    private final static String DEVKIT_BUILD = "UNNAMED.2363.ef5c8a7";
    private final static String MIN_MULE_VERSION = "3.5.0";

    /**
     * Sets proxyPass
     * 
     * @param value Value to set
     */
    public void setProxyPass(String value) {
        this.proxyPass = value;
    }

    /**
     * Retrieves proxyPass
     * 
     */
    public String getProxyPass() {
        return this.proxyPass;
    }

    /**
     * Sets username
     * 
     * @param value Value to set
     */
    public void setUsername(String value) {
        this.username = value;
    }

    /**
     * Retrieves username
     * 
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets proxyHost
     * 
     * @param value Value to set
     */
    public void setProxyHost(String value) {
        this.proxyHost = value;
    }

    /**
     * Retrieves proxyHost
     * 
     */
    public String getProxyHost() {
        return this.proxyHost;
    }

    /**
     * Sets password
     * 
     * @param value Value to set
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Retrieves password
     * 
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets proxyUser
     * 
     * @param value Value to set
     */
    public void setProxyUser(String value) {
        this.proxyUser = value;
    }

    /**
     * Retrieves proxyUser
     * 
     */
    public String getProxyUser() {
        return this.proxyUser;
    }

    /**
     * Sets url
     * 
     * @param value Value to set
     */
    public void setUrl(String value) {
        this.url = value;
    }

    /**
     * Retrieves url
     * 
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Sets muleContext
     * 
     * @param value Value to set
     */
    public void setMuleContext(MuleContext value) {
        this.muleContext = value;
    }

    /**
     * Retrieves muleContext
     * 
     */
    public MuleContext getMuleContext() {
        return this.muleContext;
    }

    /**
     * Sets poolingProfile
     * 
     * @param value Value to set
     */
    public void setPoolingProfile(PoolingProfile value) {
        this.poolingProfile = value;
    }

    /**
     * Retrieves poolingProfile
     * 
     */
    public PoolingProfile getPoolingProfile() {
        return this.poolingProfile;
    }

    /**
     * Sets retryPolicyTemplate
     * 
     * @param value Value to set
     */
    public void setRetryPolicyTemplate(RetryPolicyTemplate value) {
        this.retryPolicyTemplate = value;
    }

    /**
     * Retrieves retryPolicyTemplate
     * 
     */
    public RetryPolicyTemplate getRetryPolicyTemplate() {
        return this.retryPolicyTemplate;
    }

    public void initialise() {
        connectionPool = new DevkitGenericKeyedObjectPool(new ConnectionManagementConnectorFactory(this), poolingProfile);
        if (retryPolicyTemplate == null) {
            retryPolicyTemplate = muleContext.getRegistry().lookupObject(MuleProperties.OBJECT_DEFAULT_RETRY_POLICY_TEMPLATE);
        }
    }

    @Override
    public void dispose() {
        try {
            connectionPool.close();
        } catch (Exception e) {
        }
    }

    public AnaplanConnectorConnectionManagementAdapter acquireConnection(ConnectionManagementBasicAuthConnectionAnaplanConnectorConnectionKey key)
        throws Exception
    {
        return ((AnaplanConnectorConnectionManagementAdapter) connectionPool.borrowObject(key));
    }

    public void releaseConnection(ConnectionManagementBasicAuthConnectionAnaplanConnectorConnectionKey key, AnaplanConnectorConnectionManagementAdapter connection)
        throws Exception
    {
        connectionPool.returnObject(key, connection);
    }

    public void destroyConnection(ConnectionManagementBasicAuthConnectionAnaplanConnectorConnectionKey key, AnaplanConnectorConnectionManagementAdapter connection)
        throws Exception
    {
        connectionPool.invalidateObject(key, connection);
    }

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

    @Override
    public<P >ProcessTemplate<P, AnaplanConnectorConnectionManagementAdapter> getProcessTemplate() {
        return new ConnectionManagementProcessTemplate(this, muleContext);
    }

    @Override
    public ConnectionManagementBasicAuthConnectionAnaplanConnectorConnectionKey getDefaultConnectionKey() {
        return new ConnectionManagementBasicAuthConnectionAnaplanConnectorConnectionKey(getUsername(), getPassword(), getUrl(), getProxyHost(), getProxyUser(), getProxyPass());
    }

    @Override
    public ConnectionManagementBasicAuthConnectionAnaplanConnectorConnectionKey getEvaluatedConnectionKey(MuleEvent event)
        throws Exception
    {
        if (event!= null) {
            final String _transformedUsername = ((String) evaluateAndTransform(muleContext, event, this.getClass().getDeclaredField("username").getGenericType(), null, getUsername()));
            if (_transformedUsername == null) {
                throw new UnableToAcquireConnectionException("Parameter username in method connect can't be null because is not @Optional");
            }
            final String _transformedPassword = ((String) evaluateAndTransform(muleContext, event, this.getClass().getDeclaredField("password").getGenericType(), null, getPassword()));
            if (_transformedPassword == null) {
                throw new UnableToAcquireConnectionException("Parameter password in method connect can't be null because is not @Optional");
            }
            final String _transformedUrl = ((String) evaluateAndTransform(muleContext, event, this.getClass().getDeclaredField("url").getGenericType(), null, getUrl()));
            final String _transformedProxyHost = ((String) evaluateAndTransform(muleContext, event, this.getClass().getDeclaredField("proxyHost").getGenericType(), null, getProxyHost()));
            final String _transformedProxyUser = ((String) evaluateAndTransform(muleContext, event, this.getClass().getDeclaredField("proxyUser").getGenericType(), null, getProxyUser()));
            final String _transformedProxyPass = ((String) evaluateAndTransform(muleContext, event, this.getClass().getDeclaredField("proxyPass").getGenericType(), null, getProxyPass()));
            return new ConnectionManagementBasicAuthConnectionAnaplanConnectorConnectionKey(_transformedUsername, _transformedPassword, _transformedUrl, _transformedProxyHost, _transformedProxyUser, _transformedProxyPass);
        }
        return getDefaultConnectionKey();
    }

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

    @Override
    public ConnectionManagementBasicAuthConnectionAnaplanConnectorConnectionKey getConnectionKey(MessageProcessor messageProcessor, MuleEvent event)
        throws Exception
    {
        return getEvaluatedConnectionKey(event);
    }

    @Override
    public ConnectionManagementConnectionAdapter newConnection() {
        BasicAuthConnectionStrategyAnaplanConnectorAdapter connection = new BasicAuthConnectionStrategyAnaplanConnectorAdapter();
        return connection;
    }

    @Override
    public ConnectionManagementConnectorAdapter newConnector(ConnectionManagementConnectionAdapter<BasicAuthConnectionStrategy, ConnectionManagementBasicAuthConnectionAnaplanConnectorConnectionKey> connection) {
        AnaplanConnectorConnectionManagementAdapter connector = new AnaplanConnectorConnectionManagementAdapter();
        connector.setConnectionStrategy(connection.getStrategy());
        return connector;
    }

    public ConnectionManagementConnectionAdapter getConnectionAdapter(ConnectionManagementConnectorAdapter adapter) {
        AnaplanConnectorConnectionManagementAdapter connector = ((AnaplanConnectorConnectionManagementAdapter) adapter);
        ConnectionManagementConnectionAdapter strategy = ((ConnectionManagementConnectionAdapter) connector.getConnectionStrategy());
        return strategy;
    }

    public TestResult test() {
        AnaplanConnectorConnectionManagementAdapter connection = null;
        DefaultTestResult result;
        ConnectionManagementBasicAuthConnectionAnaplanConnectorConnectionKey key = getDefaultConnectionKey();
        try {
            connection = acquireConnection(key);
            result = new DefaultTestResult(org.mule.common.Result.Status.SUCCESS);
        } catch (Exception e) {
            try {
                destroyConnection(key, connection);
            } catch (Exception ie) {
            }
            result = ((DefaultTestResult) ConnectivityTestingErrorHandler.buildFailureTestResult(e));
        } finally {
            if (connection!= null) {
                try {
                    releaseConnection(key, connection);
                } catch (Exception ie) {
                }
            }
        }
        return result;
    }

}
