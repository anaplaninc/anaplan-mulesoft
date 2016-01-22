package com.anaplan.connector.unit;

import com.anaplan.client.Service;
import com.anaplan.client.transport.AnaplanAPITransportException;
import com.anaplan.client.transport.ApacheHttpProvider;
import com.anaplan.client.transport.TransportProvider;
import com.anaplan.client.transport.TransportProviderFactory;
import com.anaplan.connector.AnaplanConnector;
import com.anaplan.connector.connection.AnaplanConnection;
import com.anaplan.connector.connection.CertAuthConnectionStrategy;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by spondonsaha on 1/19/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({
        AnaplanConnection.class,
        CertAuthConnectionStrategy.class,
        ApacheHttpProvider.class,
        Service.class,
        TransportProvider.class,
        TransportProviderFactory.class})
public class BaseUnitTestDriver { // extends AbstractTestCase<AnaplanConnector> {

    private static Map<String, byte[]> fixtures = new HashMap<>();
    protected TransportProvider mockTransportProvider;
    private InputStream configStream;
    protected TransportProviderFactory mockTransportProviderFactory;
    protected ResourceBundle properties;
    protected Service mockService;
    protected URI mockServiceUri;
    protected static final String sampleProperties = "sample.properties";
    protected static final String certificatePath = "sample.cert";
    protected static final String workspacesResponseFile = "workspaces_response.json";
    protected String workspacesResponse;
    protected String apiUrl;


//    public BaseUnitTestDriver() {
//        super(AnaplanConnector.class);
//    }

    @Before
    public void setUpBase() {
        apiUrl = properties.getString("anaplan.apiUrl");
        configStream = getClass().getClassLoader().getResourceAsStream(sampleProperties);
        try {
            properties = new PropertyResourceBundle(getClass().getClassLoader()
                    .getResourceAsStream(sampleProperties));
            mockServiceUri = PowerMockito.spy(new URI(apiUrl));
            properties = new PropertyResourceBundle(configStream);
            workspacesResponse = new String(getFixture(workspacesResponseFile));
            mockTransportProvider = Mockito.mock(ApacheHttpProvider.class);
            mockTransportProviderFactory = Mockito.mock(TransportProviderFactory.class);
            mockService = Mockito.mock(Service.class);
            PowerMockito.whenNew(URI.class)
                        .withArguments(apiUrl)
                        .thenReturn(mockServiceUri);
            PowerMockito.whenNew(Service.class)
                        .withArguments(mockServiceUri)
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
        String path = "/1/3";  // always depending on API v1.3
        PowerMockito.doReturn(workspacesResponse)
                    .when(mockTransportProvider)
                    .get(path + "/workspaces/", "application/json");
    }

}
