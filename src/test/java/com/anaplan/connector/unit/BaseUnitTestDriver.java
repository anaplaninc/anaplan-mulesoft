package com.anaplan.connector.unit;

import com.anaplan.client.*;
import com.anaplan.client.transport.AnaplanAPITransportException;
import com.anaplan.client.transport.ApacheHttpProvider;
import com.anaplan.client.transport.TransportProvider;
import com.anaplan.client.transport.TransportProviderFactory;
import com.anaplan.connector.connection.AnaplanConnection;
import com.anaplan.connector.utils.AnaplanUtil;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
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
        Model.class,
        AnaplanUtil.class,
        Credentials.class,
        TaskStatus.class,
        TaskResult.class})
public abstract class BaseUnitTestDriver {

    private static Map<String, byte[]> fixtures = new HashMap<>();
    protected TransportProvider mockTransportProvider;
    private static InputStream configStream;
    protected TransportProviderFactory mockTransportProviderFactory;
    protected static ResourceBundle properties;
    protected Service mockService;
    protected URI serviceUri;
    protected AnaplanConnection mockAnaplanConnection;
    protected static final String sampleProperties = "test.properties";
    protected static final String workspacesResponseFile = "workspaces_response.json";
    protected static final String modelsResponseFile = "models_response.json";
    protected static final String sampleDataFilePath = "sample_data.csv";
    protected InputStream sampleDataStream;
    protected byte[] workspacesResponse;
    protected String apiUrl;
    protected static final String contentType = "application/json";
    protected static final String createTaskResponseFile = "createTask_response.json";
    protected static final String filesResponseFile = "files_response.json";
    protected TaskStatus mockStatus;
    protected TaskResult mockTaskResult;
    @Rule
    protected ExpectedException expectedEx = ExpectedException.none();


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

    @Before
    public void setUpBase() {
        try {
            sampleDataStream = new ByteArrayInputStream(getFixture(sampleDataFilePath));
            mockAnaplanConnection = Mockito.mock(AnaplanConnection.class);
            apiUrl = properties.getString("anaplan.apiUrl");
            serviceUri = new URI(apiUrl);
            workspacesResponse = getFixture(workspacesResponseFile);
            mockTransportProvider = Mockito.mock(ApacheHttpProvider.class);
            mockStatus = Mockito.mock(TaskStatus.class);
            mockTaskResult = Mockito.mock(TaskResult.class);
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
        Mockito.reset(mockAnaplanConnection);
        Mockito.reset(mockStatus);
        Mockito.reset(mockTaskResult);
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

    protected void setupMockConnection() throws Exception {
        PowerMockito.doReturn(mockService)
                .when(mockAnaplanConnection)
                .getConnection();
        mockService.setServiceCredentials(Mockito.mock(Credentials.class));
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

    protected void recordActionsTaskResultSuccess() {
        PowerMockito.doReturn(TaskStatus.State.COMPLETE).when(mockStatus)
                .getTaskState();
        PowerMockito.doReturn(mockTaskResult).when(mockStatus).getResult();
        PowerMockito.doReturn(true).when(mockTaskResult).isSuccessful();
    }

    protected void recordActionsTaskResultFailure() {
        PowerMockito.doReturn(TaskStatus.State.COMPLETE).when(mockStatus)
                .getTaskState();
        PowerMockito.doReturn(mockTaskResult).when(mockStatus).getResult();
        PowerMockito.doReturn(false).when(mockTaskResult).isSuccessful();
    }

    protected void recordActionsFetchMockItems(String entityName,
                                               String fixtureName)
                                                    throws Exception {
        PowerMockito.doReturn(getFixture(fixtureName))
                .when(mockTransportProvider)
                .get(modelUrlPathToken + "/" + entityName, "application/json");
    }

    protected void recordActionsRunServerTask(String urlPathToken)
            throws Exception {
        String taskParamsJson = "{\"localeName\":\"en_US\"}";
        PowerMockito.doReturn(getFixture(createTaskResponseFile))
                    .when(mockTransportProvider)
                    .post(urlPathToken + "/tasks", taskParamsJson.getBytes(),
                          contentType, contentType);
        PowerMockito.mockStatic(AnaplanUtil.class);
        Mockito.when(AnaplanUtil.runServerTask(Mockito.any(Task.class)))
               .thenReturn(mockStatus);
    }
}
