
package org.mule.modules.anaplan.connector.processors;

import java.lang.reflect.Type;
import javax.annotation.Generated;
import org.mule.streaming.processor.AbstractDevkitBasedPageableMessageProcessor;

@Generated(value = "Mule DevKit Version 3.5.2", date = "2015-01-02T02:35:36-08:00", comments = "Build UNNAMED.2039.0541b23")
public abstract class AbstractPagedConnectedProcessor
    extends AbstractDevkitBasedPageableMessageProcessor
    implements ConnectivityProcessor
{

    protected Object username;
    protected String _usernameType;
    protected Object password;
    protected String _passwordType;
    protected Object url;
    protected String _urlType;
    protected Object workspaceIdField;
    protected String _workspaceIdFieldType;
    protected Object modelIdField;
    protected String _modelIdFieldType;
    protected Object proxyHost;
    protected String _proxyHostType;
    protected Object proxyUser;
    protected String _proxyUserType;
    protected Object proxyPass;
    protected String _proxyPassType;

    public AbstractPagedConnectedProcessor(String operationName) {
        super(operationName);
    }

    /**
     * Sets modelIdField
     * 
     * @param value Value to set
     */
    public void setModelIdField(Object value) {
        this.modelIdField = value;
    }

    /**
     * Retrieves modelIdField
     * 
     */
    @Override
    public Object getModelIdField() {
        return this.modelIdField;
    }

    /**
     * Sets proxyPass
     * 
     * @param value Value to set
     */
    public void setProxyPass(Object value) {
        this.proxyPass = value;
    }

    /**
     * Retrieves proxyPass
     * 
     */
    @Override
    public Object getProxyPass() {
        return this.proxyPass;
    }

    /**
     * Sets username
     * 
     * @param value Value to set
     */
    public void setUsername(Object value) {
        this.username = value;
    }

    /**
     * Retrieves username
     * 
     */
    @Override
    public Object getUsername() {
        return this.username;
    }

    /**
     * Sets proxyHost
     * 
     * @param value Value to set
     */
    public void setProxyHost(Object value) {
        this.proxyHost = value;
    }

    /**
     * Retrieves proxyHost
     * 
     */
    @Override
    public Object getProxyHost() {
        return this.proxyHost;
    }

    /**
     * Sets workspaceIdField
     * 
     * @param value Value to set
     */
    public void setWorkspaceIdField(Object value) {
        this.workspaceIdField = value;
    }

    /**
     * Retrieves workspaceIdField
     * 
     */
    @Override
    public Object getWorkspaceIdField() {
        return this.workspaceIdField;
    }

    /**
     * Sets password
     * 
     * @param value Value to set
     */
    public void setPassword(Object value) {
        this.password = value;
    }

    /**
     * Retrieves password
     * 
     */
    @Override
    public Object getPassword() {
        return this.password;
    }

    /**
     * Sets proxyUser
     * 
     * @param value Value to set
     */
    public void setProxyUser(Object value) {
        this.proxyUser = value;
    }

    /**
     * Retrieves proxyUser
     * 
     */
    @Override
    public Object getProxyUser() {
        return this.proxyUser;
    }

    /**
     * Sets url
     * 
     * @param value Value to set
     */
    public void setUrl(Object value) {
        this.url = value;
    }

    /**
     * Retrieves url
     * 
     */
    @Override
    public Object getUrl() {
        return this.url;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Type typeFor(String fieldName)
        throws NoSuchFieldException
    {
        return AbstractPagedConnectedProcessor.class.getDeclaredField(fieldName).getGenericType();
    }

}
