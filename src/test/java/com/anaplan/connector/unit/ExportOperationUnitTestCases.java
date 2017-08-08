package com.anaplan.connector.unit;

import com.anaplan.client.Export;
import com.anaplan.connector.exceptions.AnaplanOperationException;
import com.anaplan.connector.utils.AnaplanExportOperation;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
@PrepareForTest({Export.class})
public class ExportOperationUnitTestCases extends BaseUnitTestDriver {

	private AnaplanExportOperation anaplanExportOperation;
    private static final String exportsResponseFile = "exports_response.json";
    private static final String exportFileChunksResponseFile = "fileChunks_response.json";
    private static final String exportUrlPathToken = properties.getString(
            "export.urlPathToken");
    private static final String exportId = properties.getString(
            "anaplan.exportId");
    private static final String filesPathToken = properties.getString(
            "exportFiles.urlPathToken");

    @Before
    public void setUp() throws Exception {
        setupMockConnection();
		anaplanExportOperation = new AnaplanExportOperation(mockAnaplanConnection);
    }

    private void recordActionsFetchMockExports() throws Exception {
        PowerMockito.doReturn(getFixture(exportsResponseFile))
                    .when(mockTransportProvider)
                    .get(modelUrlPathToken + "/exports", contentType);
    }

    private void recordActionsGetExportMetadata() throws Exception {
        PowerMockito.doReturn("{}".getBytes())
                    .when(mockTransportProvider)
                    .get(exportUrlPathToken, contentType);
    }

    private void recordActionsGetDownloadStream() throws Exception {
        PowerMockito.doReturn(getFixture(exportFileChunksResponseFile))
                    .when(mockTransportProvider)
                    .get(filesPathToken + "/chunks", contentType);
	    sampleDataStream.reset();
        PowerMockito.doReturn(IOUtils.toByteArray(sampleDataStream))
                    .when(mockTransportProvider)
                    .get(filesPathToken + "/chunks/0", null);
    }

    @Test
    public void testGoodExportCsv() throws Exception {
        // mock out API calls
        recordActionsFetchMockModels();
        recordActionsFetchMockExports();
        recordActionsRunServerTask(exportUrlPathToken);
		recordActionsGetExportMetadata();
        recordActionsTaskResultSuccess();
		recordActionsFetchMockItems("files", filesResponseFile);
        recordActionsGetDownloadStream();

        String result = anaplanExportOperation.runExport(workspaceId, modelId,
				exportId);
	    sampleDataStream.reset();
        assertEquals(new String(IOUtils.toByteArray(sampleDataStream), "UTF-8"),
	        result);
    }

    @Test
	public void testErrorFetchingModelExport() throws Exception {
		// mock out API calls
		recordActionsFetchMockModels();

		// setup Exception expectations
		expectedEx.expect(AnaplanOperationException.class);
		expectedEx.expectMessage("Error fetching Export action:");

		anaplanExportOperation.runExport(workspaceId, modelId, exportId);
	}

	@Test
	public void testFetchNullModelExport() throws Exception {
		// mock out API calls
		recordActionsFetchMockModels();
		recordActionsFetchMockExports();

		// setup Exception expectations
		expectedEx.expect(AnaplanOperationException.class);
		expectedEx.expectMessage("Invalid export Id: badExportId");

		anaplanExportOperation.runExport(workspaceId, modelId, "badExportId");
	}

	@Test
	public void testErrorRunningTask() throws Exception {
		// mock out API calls
		recordActionsFetchMockModels();
		recordActionsFetchMockExports();
		recordActionsFetchMockItems("files", filesResponseFile);

		// setup Exception expectations
		expectedEx.expect(AnaplanOperationException.class);
		expectedEx.expectMessage("Error running Export action:");

		anaplanExportOperation.runExport(workspaceId, modelId, exportId);
	}

	@Test
	public void testErrorFetchExportMetadata() throws Exception {
		// mock out API calls
		recordActionsFetchMockModels();
		recordActionsFetchMockExports();
		recordActionsFetchMockItems("files", filesResponseFile);
		recordActionsRunServerTask(exportUrlPathToken);

		// setup Exception expectations
		expectedEx.expect(AnaplanOperationException.class);
		expectedEx.expectMessage("Error fetching Export-metadata!");

		anaplanExportOperation.runExport(workspaceId, modelId, exportId);
	}

	@Test
	public void testFetchNullExportServerFile() throws Exception {
		// mock out API calls
		recordActionsFetchMockModels();
		recordActionsFetchMockExports();
		recordActionsRunServerTask(exportUrlPathToken);
		recordActionsGetExportMetadata();
		recordActionsTaskResultSuccess();

		// setup Exception expectations
		expectedEx.expect(AnaplanOperationException.class);
		expectedEx.expectMessage("Error fetching export Server-File:");

		anaplanExportOperation.runExport(workspaceId, modelId, exportId);
	}

	@Test
	public void testBadExport() throws Exception {
		// mock out API calls
		recordActionsFetchMockModels();
		recordActionsFetchMockExports();
		recordActionsRunServerTask(exportUrlPathToken);
		recordActionsGetExportMetadata();
		recordActionsTaskResultFailure();

		// setup Exception expectations
		expectedEx.expect(AnaplanOperationException.class);
		expectedEx.expectMessage("Operation failed!");

		anaplanExportOperation.runExport(workspaceId, modelId, exportId);
	}
}
