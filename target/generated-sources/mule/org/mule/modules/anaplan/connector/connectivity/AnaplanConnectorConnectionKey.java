
package org.mule.modules.anaplan.connector.connectivity;

import javax.annotation.Generated;


/**
 * A tuple of connection parameters
 * 
 */
@Generated(value = "Mule DevKit Version 3.5.2", date = "2015-01-08T02:50:03-08:00", comments = "Build UNNAMED.2039.0541b23")
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
    private String workspaceId;
    /**
     * 
     */
    private String modelId;
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

    public AnaplanConnectorConnectionKey(String username, String password, String url, String workspaceId, String modelId, String proxyHost, String proxyUser, String proxyPass) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.workspaceId = workspaceId;
        this.modelId = modelId;
        this.proxyHost = proxyHost;
        this.proxyUser = proxyUser;
        this.proxyPass = proxyPass;
    }

    /**
     * Sets modelId
     * 
     * @param value Value to set
     */
    public void setModelId(String value) {
        this.modelId = value;
    }

    /**
     * Retrieves modelId
     * 
     */
    public String getModelId() {
        return this.modelId;
    }

    /**
     * Sets workspaceId
     * 
     * @param value Value to set
     */
    public void setWorkspaceId(String value) {
        this.workspaceId = value;
    }

    /**
     * Retrieves workspaceId
     * 
     */
    public String getWorkspaceId() {
        return this.workspaceId;
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
