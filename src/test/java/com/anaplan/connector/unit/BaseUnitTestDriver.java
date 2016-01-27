package com.anaplan.connector.unit;

import com.anaplan.client.Model;
import com.anaplan.client.Service;
import com.anaplan.client.Workspace;
import com.anaplan.client.transport.AnaplanAPITransportException;
import com.anaplan.client.transport.ApacheHttpProvider;
import com.anaplan.client.transport.TransportProvider;
import com.anaplan.client.transport.TransportProviderFactory;
import com.anaplan.connector.connection.AnaplanConnection;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({
        AnaplanConnection.class,
        ApacheHttpProvider.class,
        Service.class,
        TransportProvider.class,
        TransportProviderFactory.class,
        URI.class,
        Workspace.class,
        Model.class})
public abstract class BaseUnitTestDriver {

    private static Map<String, byte[]> fixtures = new HashMap<>();
    protected TransportProvider mockTransportProvider;
    private static InputStream configStream;
    protected TransportProviderFactory mockTransportProviderFactory;
    protected static ResourceBundle properties;
    protected Service mockService;
    protected URI serviceUri;
    protected static final String sampleProperties = "test.properties";
    protected static final String workspacesResponseFile = "workspaces_response.json";
    protected static final String modelsResponseFile = "models_response.json";
    protected static final String sampleDataFile = "sample_data.csv";
    protected byte[] workspacesResponse;
    protected String apiUrl;

    static {
        configStream = BaseUnitTestDriver.class.getClassLoader()
                .getResourceAsStream(sampleProperties);
        try {
            properties = new PropertyResourceBundle(
                    BaseUnitTestDriver.class.getClassLoader()
                            .getResourceAsStream(sampleProperties));
            properties = new PropertyResourceBundle(configStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties!");
        }
    }

    protected static final String workspaceUrlPathToken = properties.getString(
            "workspace.urlPathToken");  // API v1.3
    protected static final String modelUrlPathToken = properties.getString(
            "model.urlPathToken");
    protected static final String workspaceId = properties.getString(
            "anaplan.workspaceId");
    protected static final String modelId = properties.getString(
            "anaplan.modelId");
    protected static final String certificatePath = properties.getString(
            "certificate.path");
    protected static final String testUsername = properties.getString("anaplan.username");

    @Before
    public void setUpBase() {
        try {
            apiUrl = properties.getString("anaplan.apiUrl");
            serviceUri = new URI(apiUrl);
            workspacesResponse = getFixture(workspacesResponseFile);
            mockTransportProvider = Mockito.mock(ApacheHttpProvider.class);
            mockTransportProviderFactory = Mockito.mock(TransportProviderFactory.class);
            mockService = PowerMockito.spy(new Service(serviceUri));
            PowerMockito.whenNew(URI.class)
                        .withArguments(apiUrl)
                        .thenReturn(serviceUri);
            PowerMockito.whenNew(Service.class)
                        .withArguments(serviceUri)
                        .thenReturn(mockService);
            recordActionsFetchMockTransportProvider();
            recordActionsFetchMockWorkspaces();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void tearDownBase() throws IOException {
        Mockito.reset(mockTransportProvider);
        Mockito.reset(mockTransportProviderFactory);
        Mockito.reset(mockService);
    }

    /**
     * Lazily loads fixtures.
     * @param fixtureName Name of the file to load.
     * @return Fixture data as a Byte array
     * @throws IOException
     */
    protected byte[] getFixture(String fixtureName) {
        InputStream fixtureStream;
        if (!fixtures.containsKey(fixtureName)) {
            fixtureStream = getClass().getClassLoader().getResourceAsStream(
                    fixtureName);
            byte[] streamBytes;
            try {
                streamBytes = IOUtils.toByteArray(fixtureStream);
            } catch (IOException e) {
                throw new RuntimeException("Could not convert to Byte array!", e);
            }
            fixtures.put(fixtureName, streamBytes);
        }
        return fixtures.get(fixtureName);
    }

    protected void recordActionsFetchMockTransportProvider()
            throws AnaplanAPITransportException {
        PowerMockito.mockStatic(TransportProviderFactory.class);
        Mockito.when(TransportProviderFactory.getInstance())
               .thenReturn(mockTransportProviderFactory);
        PowerMockito.doReturn(mockTransportProvider)
                    .when(mockTransportProviderFactory)
                    .createDefaultProvider();
    }

    protected void recordActionsFetchMockWorkspaces()
            throws AnaplanAPITransportException, IOException {
        PowerMockito.doReturn(workspacesResponse)
                    .when(mockTransportProvider)
                    .get(workspaceUrlPathToken, "application/json");
    }

    protected void recordActionsFetchMockWorkspaceFail()
            throws AnaplanAPITransportException, IOException {
        PowerMockito.doThrow(new AnaplanAPITransportException("Test exception"))
                    .when(mockTransportProvider)
                    .get(workspaceUrlPathToken, "application/json");
    }

    protected void recordActionsFetchWorkspacesEmptyResult()
            throws AnaplanAPITransportException, IOException {
        PowerMockito.doReturn(IOUtils.toByteArray("[]"))
                    .when(mockTransportProvider)
                    .get(workspaceUrlPathToken, "application/json");
    }

    protected void recordActionsFetchMockModels() throws Exception {
        String uriToken = workspaceUrlPathToken
                + properties.getString("anaplan.workspaceId")
                + "/models";
        PowerMockito.doReturn(getFixture(modelsResponseFile))
                .when(mockTransportProvider)
                .get(uriToken, "application/json");
    }

    protected void recordActionsFetchMockItems(String entityName,
                                               String fixtureName)
                                                    throws Exception {
        PowerMockito.doReturn(getFixture(fixtureName))
                .when(mockTransportProvider)
                .get(modelUrlPathToken + "/" + entityName, "application/json");
    }
}
