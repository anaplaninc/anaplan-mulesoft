package com.anaplan.connector.unit;

import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.Service;
import com.anaplan.client.Workspace;
import com.anaplan.connector.AnaplanConnectorProperties;
import com.anaplan.connector.connection.AnaplanConnection;
import com.anaplan.connector.connection.BasicAuthConnectionStrategy;
import com.anaplan.connector.connection.CertAuthConnectionStrategy;
import com.anaplan.connector.exceptions.AnaplanConnectionException;
import com.anaplan.connector.exceptions.ConnectorPropertiesException;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest({
        CertAuthConnectionStrategy.class,
        BasicAuthConnectionStrategy.class,
        Service.class})
public class ConnectionUnitTestCases extends BaseUnitTestDriver {

    /**
     * Helper method that validates the connection by fetching list of user's
     * workspaces, which makes an authenticated API call.
     * @param connection Service connection object to introspect.
     */
    public void validateConnection(AnaplanConnection connection) {
        Service testService = connection.getConnection();
        List<Workspace> workspaces = new ArrayList<>();
        try {
            workspaces = testService.getWorkspaces();
        } catch (AnaplanAPIException e) {
            fail("Error getting workspaces!");
        }
        assertEquals(workspaces.size(), 4);
        assertNotNull(connection.getConnection());
        assertEquals(connection.getConnection(), mockService);
    }

    @Test
    public void testBasicConnection() throws Exception {
        BasicAuthConnectionStrategy basicAuth = PowerMockito.spy(
                new BasicAuthConnectionStrategy());
        basicAuth.connect("username", "password", apiUrl, "proxyHost",
                "proxyUser", "proxyPass");
        validateConnection(basicAuth.getApiConnection());
    }

    @Test
    public void testCertificateConnection() throws Exception {
        CertAuthConnectionStrategy certAuth = PowerMockito.spy(
                new CertAuthConnectionStrategy());
        certAuth.connect(certificatePath, apiUrl, "proxyHost", "proxyUser",
                "proxyPass");
        validateConnection(certAuth.getApiConnection());
    }

    @Test
    public void testBadCertificate() throws Exception {
        CertAuthConnectionStrategy certAuth = PowerMockito.spy(
                new CertAuthConnectionStrategy());
        expectedEx.expect(org.mule.api.ConnectionException.class);
        expectedEx.expectMessage("Could not open certificate");
        certAuth.connect("not/a/path/to/cert", apiUrl, "proxyHost", "proxyUser",
                "proxyPass");
    }

    @Test
    public void testFetchWorkspacesFailWhileTryingToAuth() throws Exception {
        recordActionsFetchMockWorkspaceFail();
        expectedEx.expect(org.mule.api.ConnectionException.class);
        expectedEx.expectMessage("Test exception");
        CertAuthConnectionStrategy certAuth = PowerMockito.spy(
                new CertAuthConnectionStrategy());
        certAuth.connect(certificatePath, apiUrl, "proxyHost", "proxyUser",
                "proxyPass");
    }

    @Test
    public void testFetchWorkspacesEmptyResponse() throws Exception {
        recordActionsFetchWorkspacesEmptyResult();
        expectedEx.expect(org.mule.api.ConnectionException.class);
        expectedEx.expectMessage("Wrong username or password, or no " +
                "workspaces accessible");
        CertAuthConnectionStrategy certAuth = PowerMockito.spy(
                new CertAuthConnectionStrategy());
        certAuth.connect(certificatePath, apiUrl, "proxyHost", "proxyUser",
                "proxyPass");
    }

	@Test
	public void testConnection() throws Exception {
		// validate AnaplanConnection is set null before connect()
		CertAuthConnectionStrategy certAuth = PowerMockito.spy(
				new CertAuthConnectionStrategy());
		certAuth.disconnect();
		assertNull(certAuth.getApiConnection());
		assertEquals("Not connected!", certAuth.connectionId());

		// validate after connecting, AnaplanConnection object is present
		certAuth.connect(certificatePath, apiUrl, "proxyHost", "proxyUser",
				"proxyPass");
		validateConnection(certAuth.getApiConnection());
		assertNotNull(certAuth.getApiConnection());
		assertThat(certAuth.connectionId(), CoreMatchers.containsString(
				"com.anaplan.connector.connection.AnaplanConnection@"));

		// disconnect and re-validate if the AnaplanConnection object is present
		certAuth.disconnect();
		assertNull(certAuth.getApiConnection());
		assertEquals("Not connected!", certAuth.connectionId());
	}

	@Test
	public void testConnectionValidator_NoConnection() throws Exception {
		CertAuthConnectionStrategy certAuth = PowerMockito.spy(
				new CertAuthConnectionStrategy());
		expectedEx.expect(AnaplanConnectionException.class);
		expectedEx.expectMessage("No connStrategy object: call connect()");
		certAuth.validateConnection();
	}

	@Test
	public void testConnectionValidator_createNewConnection() throws Exception {
		CertAuthConnectionStrategy certAuth = PowerMockito.spy(
				new CertAuthConnectionStrategy());
		certAuth.connect(certificatePath, apiUrl, "proxyHost", "proxyUser",
				"proxyPass");
		certAuth.validateConnection();
		assertNotNull(certAuth.getApiConnection());
	}

	@Test
	public void testConnectorProperties() throws Exception {
		AnaplanConnectorProperties props = new AnaplanConnectorProperties();
		expectedEx.expect(ConnectorPropertiesException.class);
		expectedEx.expectMessage("Provided field-values and required " +
				"property-fields are of different lengths!!");
		String[] badCreds = new String[]{"cred1", "cred2"};
		props.setProperties(badCreds, "only_cred");
	}
}
