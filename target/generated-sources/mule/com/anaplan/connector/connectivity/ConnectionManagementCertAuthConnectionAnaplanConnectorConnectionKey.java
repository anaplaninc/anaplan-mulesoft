
package com.anaplan.connector.connectivity;

import javax.annotation.Generated;
import org.mule.devkit.shade.connection.management.ConnectionManagementConnectionKey;

@Generated(value = "Mule DevKit Version 3.6.0", date = "2015-04-20T09:44:38-07:00", comments = "Build UNNAMED.2363.ef5c8a7")
public class ConnectionManagementCertAuthConnectionAnaplanConnectorConnectionKey implements ConnectionManagementConnectionKey
{

    /**
     * 
     */
    private String certificatePath;
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

    public ConnectionManagementCertAuthConnectionAnaplanConnectorConnectionKey(String certificatePath, String url, String proxyHost, String proxyUser, String proxyPass) {
        this.certificatePath = certificatePath;
        this.url = url;
        this.proxyHost = proxyHost;
        this.proxyUser = proxyUser;
        this.proxyPass = proxyPass;
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
     * Sets certificatePath
     * 
     * @param value Value to set
     */
    public void setCertificatePath(String value) {
        this.certificatePath = value;
    }

    /**
     * Retrieves certificatePath
     * 
     */
    public String getCertificatePath() {
        return this.certificatePath;
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
        int result = ((this.certificatePath!= null)?this.certificatePath.hashCode(): 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConnectionManagementCertAuthConnectionAnaplanConnectorConnectionKey)) {
            return false;
        }
        ConnectionManagementCertAuthConnectionAnaplanConnectorConnectionKey that = ((ConnectionManagementCertAuthConnectionAnaplanConnectorConnectionKey) o);
        if (((this.certificatePath!= null)?(!this.certificatePath.equals(that.certificatePath)):(that.certificatePath!= null))) {
            return false;
        }
        return true;
    }

}
