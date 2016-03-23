package com.anaplan.connector.unit;

import com.anaplan.client.ServerFile;
import com.anaplan.connector.exceptions.AnaplanOperationException;
import com.anaplan.connector.utils.AnaplanImportOperation;
import com.anaplan.connector.utils.AnaplanUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
@PrepareForTest({
        ServerFile.class})
public class ImportOperationUnitTestCases extends BaseUnitTestDriver {

    private static final String importId = properties.getString(
            "anaplan.importId");
    private static final String csvColumnSeparator = properties.getString(
            "anaplan.columnSeparatorCOMMA");
    private static final String csvDelimiter = properties.getString(
            "anaplan.delimiter");
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

	private void recordActionsStrinkChunkReader() {
		PowerMockito.when(AnaplanUtil.stringChunkReader(Mockito.anyString()))
				.thenCallRealMethod();
		PowerMockito.when(AnaplanUtil.stringChunkReader(Mockito.anyString(),
				Mockito.anyInt())).thenCallRealMethod();
	}

    @Test
    public void testGoodImportCsv() throws Exception {
        // mock out API calls
        recordActionsFetchMockModels();
        recordActionsFetchMockImports();
        recordActionsFetchMockItems("files", filesResponseFile);
        recordActionsRunServerTask(importUrlPathToken);
        recordActionsImportTaskResultSuccess();
		recordActionsStrinkChunkReader();

        anaplanImportOperation.runImport(sampleDataFile, workspaceId, modelId,
                importId, csvColumnSeparator, csvDelimiter);
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
		recordActionsStrinkChunkReader();
		recordActionsImportTaskResultFailureDump();

		String response = anaplanImportOperation.runImport(sampleDataFile,
				workspaceId, modelId, importId, csvColumnSeparator, csvDelimiter);
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

        anaplanImportOperation.runImport(sampleDataFile, workspaceId, modelId,
                importId, csvColumnSeparator, csvDelimiter);
    }

    @Test
    public void testFetchNullModelImport() throws Exception {
        // mock out API calls
        recordActionsFetchMockModels();
        recordActionsFetchMockImports();

        // setup Exception expectations
        expectedEx.expect(AnaplanOperationException.class);
        expectedEx.expectMessage("Invalid import ID provided: badImportId");

        anaplanImportOperation.runImport(sampleDataFile,
                workspaceId, modelId, "badImportId", csvColumnSeparator,
                csvDelimiter);
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

        anaplanImportOperation.runImport(sampleDataFile, workspaceId, modelId,
                importId, csvColumnSeparator, csvDelimiter);
    }

}
