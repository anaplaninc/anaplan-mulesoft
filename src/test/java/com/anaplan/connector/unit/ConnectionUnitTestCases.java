package com.anaplan.connector.unit;

import com.anaplan.client.Service;
import com.anaplan.client.transport.ApacheHttpProvider;
import com.anaplan.client.transport.TransportProvider;
import com.anaplan.client.transport.TransportProviderFactory;
import com.anaplan.connector.connection.AnaplanConnection;
import com.anaplan.connector.connection.BasicAuthConnectionStrategy;
import com.anaplan.connector.connection.CertAuthConnectionStrategy;
import org.apache.cxf.common.i18n.Exception;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
@PrepareForTest({
        AnaplanConnection.class,
        CertAuthConnectionStrategy.class,
        ApacheHttpProvider.class,
        Service.class,
        TransportProvider.class,
        TransportProviderFactory.class})
public class ConnectionUnitTestCases extends BaseUnitTestDriver {

    private String sampleFileData;
    private static final String sampleDataFile = "sample_data.csv";
    private AnaplanConnection anaplanConnection;
    private CertAuthConnectionStrategy mockCertAuth;
    private BasicAuthConnectionStrategy mockBasicAuth;

    @Before
    public void setUp() throws Exception {
        sampleFileData = new String(getFixture(sampleDataFile));
        mockBasicAuth = PowerMockito.spy(new BasicAuthConnectionStrategy());
        mockCertAuth = PowerMockito.spy(new CertAuthConnectionStrategy());
    }

    @After
    public void tearDown() {
        Mockito.reset(mockCertAuth);
    }

    @Test
    public void testBasicConnection() throws org.mule.api.ConnectionException {
//        mockBasicAuth.connect("username", "password", "url", "proxyHost",
//                "proxyUser", "proxyPass");
        assertEquals(1, 2);
    }

    @Test
    public void testCertificateConnection() throws Exception {
//        try {
//            CertAuthConnectionStrategy certAuth = new CertAuthConnectionStrategy();
//        } catch (NoClassDefFoundError e) {
//            throw new Exception(e);
//        }

    }

}
