
package org.mule.modules.anaplan.connector.connectivity;

import javax.annotation.Generated;


/**
 * A tuple of connection parameters
 * 
 */
@Generated(value = "Mule DevKit Version 3.5.2", date = "2015-01-02T02:35:36-08:00", comments = "Build UNNAMED.2039.0541b23")
public class AnaplanConnectorConnectionKey {

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
    private String workspaceIdField;
    /**
     * 
     */
    private String modelIdField;
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

    public AnaplanConnectorConnectionKey(String username, String password, String url, String workspaceIdField, String modelIdField, String proxyHost, String proxyUser, String proxyPass) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.workspaceIdField = workspaceIdField;
        this.modelIdField = modelIdField;
        this.proxyHost = proxyHost;
        this.proxyUser = proxyUser;
        this.proxyPass = proxyPass;
    }

    /**
     * Sets modelIdField
     * 
     * @param value Value to set
     */
    public void setModelIdField(String value) {
        this.modelIdField = value;
    }

    /**
     * Retrieves modelIdField
     * 
     */
    public String getModelIdField() {
        return this.modelIdField;
    }

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
     * Sets workspaceIdField
     * 
     * @param value Value to set
     */
    public void setWorkspaceIdField(String value) {
        this.workspaceIdField = value;
    }

    /**
     * Retrieves workspaceIdField
     * 
     */
    public String getWorkspaceIdField() {
        return this.workspaceIdField;
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

    @Override
    public int hashCode() {
        int result = ((this.username!= null)?this.username.hashCode(): 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnaplanConnectorConnectionKey)) {
            return false;
        }
        AnaplanConnectorConnectionKey that = ((AnaplanConnectorConnectionKey) o);
        if (((this.username!= null)?(!this.username.equals(that.username)):(that.username!= null))) {
            return false;
        }
        return true;
    }

}
