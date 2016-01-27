package com.anaplan.connector.unit;

import com.anaplan.client.Export;
import com.anaplan.client.TaskStatus;
import com.anaplan.connector.utils.AnaplanExportOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
@PrepareForTest({Export.class})
public class ExportOperationTestCases extends BaseUnitTestDriver {

    private static final String exportsResponseFile = "exports_response.json";
    private static final String exportFileChunksResponseFile = "export_fileChunks_response.json";
    private static final String exportUrlPathToken = properties.getString(
            "export.urlPathToken");
    private static final String exportId = properties.getString(
            "anaplan.exportId");
    private static final String filesPathToken = properties.getString(
            "exportFiles.urlPathToken");

    @Before
    public void setUp() throws Exception {
        setupMockConnection();
    }

    private void recordActionsFetchMockExports() throws Exception {
        PowerMockito.doReturn(getFixture(exportsResponseFile))
                    .when(mockTransportProvider)
                    .get(modelUrlPathToken + "/exports", contentType);
    }

    private void recordActionsExportTaskResultSuccess() {
        PowerMockito.doReturn(TaskStatus.State.COMPLETE).when(mockStatus)
                    .getTaskState();
        PowerMockito.doReturn(mockTaskResult).when(mockStatus).getResult();
        PowerMockito.doReturn(true).when(mockTaskResult).isSuccessful();
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
        PowerMockito.doReturn(sampleDataFile.getBytes())
                    .when(mockTransportProvider)
                    .get(filesPathToken + "/chunks/0", null);
    }

    @Test
    public void testGoodExportCsv() throws Exception {
        // mock out API calls
        recordActionsFetchMockModels();
        recordActionsFetchMockExports();
        recordActionsFetchMockItems("files", filesResponseFile);
        recordActionsRunServerTask(exportUrlPathToken);
        recordActionsExportTaskResultSuccess();
        recordActionsGetExportMetadata();
        recordActionsGetDownloadStream();

        AnaplanExportOperation exportOp = new AnaplanExportOperation(
                mockAnaplanConnection);
        String result = exportOp.runExport(workspaceId, modelId, exportId);
        assertEquals(sampleDataFile, result);
    }

}
