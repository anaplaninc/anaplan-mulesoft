package com.anaplan.connector.unit;

import com.anaplan.connector.utils.AnaplanProcessOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
public class ProcessOperationUnitTestCases extends BaseUnitTestDriver {

    private static final String processesResponseFile = "processes_response.json";
    private static final String processUrlPathToken = properties.getString(
            "process.urlPathToken");
    private static final String processId = properties.getString(
            "anaplan.processId");

    @Before
    public void setUp() throws Exception {
        setupMockConnection();
    }

    private void recordActionsFetchMockProcesses() throws Exception {
        PowerMockito.doReturn(getFixture(processesResponseFile))
                    .when(mockTransportProvider)
                    .get(modelUrlPathToken + "/processes", contentType);
    }

    @Test
    public void testProcessGoodCase() throws Exception {
        // mock out API calls
        recordActionsFetchMockModels();
        recordActionsFetchMockProcesses();
        recordActionsFetchMockItems("files", filesResponseFile);
        recordActionsRunServerTask(processUrlPathToken);
        recordActionsTaskResultSuccess();

        AnaplanProcessOperation processOp = new AnaplanProcessOperation(
                mockAnaplanConnection);
        String result = processOp.runProcess(workspaceId, modelId, processId);
        assertEquals("[processId] completed successfully!\n\n", result);
    }
}
