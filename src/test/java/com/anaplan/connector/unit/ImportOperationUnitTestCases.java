package com.anaplan.connector.unit;

import com.anaplan.client.ServerFile;
import com.anaplan.connector.exceptions.AnaplanOperationException;
import com.anaplan.connector.utils.AnaplanImportOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.FilterOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
@PrepareForTest({
        ServerFile.class})
public class ImportOperationUnitTestCases extends BaseUnitTestDriver {

    private static final String importId = properties.getString(
            "anaplan.importId");
    private static final String importUrlPathToken = properties.getString(
            "import.urlPathToken");
	private static final String dumpFileUrlPathToken = properties.getString(
			"dumpFile.urlPathToken");
    private static final String importsResponse = "imports_response.json";
	private static final String dumpFileResponse = "dump_file_response.csv";
	private OutputStream mockOutputStream;
    private AnaplanImportOperation anaplanImportOperation;
	private ServerFile mockFailDumpServerFile;

    @Before
    public void setUp() throws Exception {
        mockOutputStream = Mockito.mock(FilterOutputStream.class);
        setupMockConnection();
        anaplanImportOperation = new AnaplanImportOperation(mockAnaplanConnection);
		mockFailDumpServerFile = Mockito.mock(ServerFile.class);
    }

    @After
    public void tearDown() {
        Mockito.reset(mockOutputStream);
		Mockito.reset(mockFailDumpServerFile);
    }

    private void recordActionsFetchMockImports() throws Exception {
        PowerMockito.doReturn(getFixture(importsResponse))
                    .when(mockTransportProvider)
                    .get(modelUrlPathToken + "/imports", contentType);
    }

    private void recordActionsImportTaskResultSuccess() {
        PowerMockito.doReturn(mockTaskResult).when(mockStatus).getResult();
        PowerMockito.doReturn(null).when(mockTaskResult).getDetails();
        PowerMockito.doReturn(false).when(mockTaskResult).isFailureDumpAvailable();
        PowerMockito.doReturn(true).when(mockTaskResult).isSuccessful();
    }

    @Test
    public void testGoodImportCsv() throws Exception {
        // mock out API calls
        recordActionsFetchMockModels();
        recordActionsFetchMockImports();
        recordActionsFetchMockItems("files", filesResponseFile);
        recordActionsRunServerTask(importUrlPathToken);
        recordActionsImportTaskResultSuccess();

        anaplanImportOperation.runImport(sampleDataStream, workspaceId, modelId,
                importId, null);
    }

	private void recordActionsImportTaskResultFailureDump() throws Exception {
		PowerMockito.doReturn(mockTaskResult).when(mockStatus).getResult();
		PowerMockito.doReturn(null).when(mockTaskResult).getDetails();
		PowerMockito.doReturn(true).when(mockTaskResult).isFailureDumpAvailable();
		PowerMockito.doReturn(mockFailDumpServerFile)
					.when(mockTaskResult).getFailureDump();
		InputStream dumpFileStream = new ByteArrayInputStream(
				getFixture(dumpFileResponse));
		dumpFileStream.close();
		PowerMockito.doReturn(dumpFileStream).when(mockFailDumpServerFile)
					.getDownloadStream();
	}

    @Test
    public void testImportWithFailureDump() throws Exception {
		// mock out API calls
		recordActionsFetchMockModels();
		recordActionsFetchMockImports();
		recordActionsFetchMockItems("files", filesResponseFile);
		recordActionsRunServerTask(importUrlPathToken);
		recordActionsImportTaskResultFailureDump();

		String response = anaplanImportOperation.runImport(sampleDataStream,
				workspaceId, modelId, importId, null);
		String expectedResponseMsg = "Operation ran successfully but with warnings!\n" +
				"Response Message:\nSome records were not imported: check " +
				"connector output data for details: importId\nDump File " +
				"contents:\n" + new String(getFixture(dumpFileResponse));
		assertEquals(expectedResponseMsg, response);
	}

    @Test
    public void testErrorFetchingModelImport() throws Exception {
        // mock out API calls
        recordActionsFetchMockModels();

        // setup Exception expectations
        expectedEx.expect(AnaplanOperationException.class);
        expectedEx.expectMessage("Error fetching Import action:");

        anaplanImportOperation.runImport(sampleDataStream, workspaceId, modelId,
                importId, null);
    }

    @Test
    public void testFetchNullModelImport() throws Exception {
        // mock out API calls
        recordActionsFetchMockModels();
        recordActionsFetchMockImports();

        // setup Exception expectations
        expectedEx.expect(AnaplanOperationException.class);
        expectedEx.expectMessage("Invalid import ID provided: badImportId");

        anaplanImportOperation.runImport(sampleDataStream,
                workspaceId, modelId, "badImportId", null);
    }

    @Test
    public void testErrorRunningTask() throws Exception {
        // mock out API calls
        recordActionsFetchMockModels();
        recordActionsFetchMockImports();
        recordActionsFetchMockItems("files", filesResponseFile);

        // setup Exception expectations
        expectedEx.expect(AnaplanOperationException.class);
        expectedEx.expectMessage("Error running Import action:");

        anaplanImportOperation.runImport(sampleDataStream, workspaceId, modelId,
                importId, null);
    }

}
