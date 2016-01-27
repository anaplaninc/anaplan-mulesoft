package com.anaplan.connector.unit;

import com.anaplan.client.*;
import com.anaplan.connector.connection.AnaplanConnection;
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

import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.OutputStream;


/**
 * Created by spondonsaha on 1/25/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({
        Credentials.class,
        ServerFile.class,
        FileOutputStream.class,
        TaskStatus.class,
        Task.class,
        TaskResult.class,
        AnaplanUtil.class})
public class ImportOperationTestCases extends BaseUnitTestDriver {

    private static final String importId = "importId";
    private static final String csvColumnSeparator = properties.getString(
            "anaplan.columnSeparatorCOMMA");
    private static final String csvDelimiter = properties.getString(
            "anaplan.delimiter");
    private static final String importUrlPathToken = properties.getString(
            "import.urlPathToken");
    private static final String importsResponseFile = "imports_response.json";
    private static final String filesResponseFile = "files_response.json";
    private static final String createTaskResponseFile = "createTask_response.json";
    private String sampleFileData;
    private ServerFile mockServerFile;
    private OutputStream mockOutputStream;
    private AnaplanConnection mockAnaplanConnection;
    private TaskStatus mockStatus;
    private Task mockTask;
    private TaskResult mockTaskResult;

    @Before
    public void setUp() throws Exception {
        mockAnaplanConnection = Mockito.mock(AnaplanConnection.class);
        sampleFileData = new String(getFixture(sampleDataFile));
        mockServerFile = Mockito.mock(ServerFile.class);
        mockOutputStream = Mockito.mock(FilterOutputStream.class);
        setupMockConnection();
        mockTask = Mockito.mock(Task.class);
        mockStatus = Mockito.mock(TaskStatus.class);
        mockTaskResult = Mockito.mock(TaskResult.class);
    }

    private void setupMockConnection() throws Exception {
        PowerMockito.doReturn(mockService)
                    .when(mockAnaplanConnection)
                    .getConnection();
        mockService.setServiceCredentials(Mockito.mock(Credentials.class));
    }

    @After
    public void tearDown() {
        Mockito.reset(mockAnaplanConnection);
        Mockito.reset(mockServerFile);
        Mockito.reset(mockOutputStream);
        Mockito.reset(mockTask);
        Mockito.reset(mockStatus);
        Mockito.reset(mockTaskResult);
    }

    private void recordActionsFetchMockImports() throws Exception {
        PowerMockito.doReturn(getFixture(importsResponseFile))
                    .when(mockTransportProvider)
                    .get(modelUrlPathToken + "/imports", "application/json");
    }

    private void recordActionsRunServerTask() throws Exception {
        String contentType = "application/json";
        String taskParamsJson = "{\"localeName\":\"en_US\"}";
        PowerMockito.doReturn(getFixture(createTaskResponseFile))
                    .when(mockTransportProvider)
                    .post(importUrlPathToken + "/tasks", taskParamsJson.getBytes(),
                            contentType, contentType);
        PowerMockito.mockStatic(AnaplanUtil.class);
        Mockito.when(AnaplanUtil.runServerTask(Mockito.any(Task.class)))
               .thenReturn(mockStatus);
    }

    private void recordActionsProcessTaskResultSuccess() {
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
        recordActionsRunServerTask();
        recordActionsProcessTaskResultSuccess();

        AnaplanImportOperation importOp = new AnaplanImportOperation(mockAnaplanConnection);
        importOp.runImport(sampleFileData, workspaceId, modelId, importId,
                csvColumnSeparator, csvDelimiter);
    }
}
