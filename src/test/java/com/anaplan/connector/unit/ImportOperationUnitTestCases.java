package com.anaplan.connector.unit;

import com.anaplan.connector.utils.AnaplanImportOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FilterOutputStream;
import java.io.OutputStream;


@RunWith(PowerMockRunner.class)
public class ImportOperationUnitTestCases extends BaseUnitTestDriver {

    private static final String importId = properties.getString(
            "anaplan.importId");
    private static final String csvColumnSeparator = properties.getString(
            "anaplan.columnSeparatorCOMMA");
    private static final String csvDelimiter = properties.getString(
            "anaplan.delimiter");
    private static final String importUrlPathToken = properties.getString(
            "import.urlPathToken");
    private static final String importsResponseFile = "imports_response.json";
    private OutputStream mockOutputStream;

    @Before
    public void setUp() throws Exception {
        mockOutputStream = Mockito.mock(FilterOutputStream.class);
        setupMockConnection();
    }

    @After
    public void tearDown() {
        Mockito.reset(mockOutputStream);
    }

    private void recordActionsFetchMockImports() throws Exception {
        PowerMockito.doReturn(getFixture(importsResponseFile))
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

        AnaplanImportOperation importOp = new AnaplanImportOperation(
                mockAnaplanConnection);
        importOp.runImport(sampleDataFile, workspaceId, modelId, importId,
                csvColumnSeparator, csvDelimiter);
    }
}
