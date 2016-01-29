package com.anaplan.connector.unit;


import com.anaplan.connector.utils.AnaplanDeleteOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
public class DeleteOperationUnitTestCases extends BaseUnitTestDriver {

    private static final String deletesResponseFile = "actions_response.json";
    private static final String deleteUrlPathToken = properties.getString(
            "delete.urlPathToken");
    private static final String deleteId = properties.getString(
            "anaplan.deleteId");

    @Before
    public void setUp() throws Exception {
        setupMockConnection();
    }

    private void recordActionsFetchMockDeletes() throws Exception {
        PowerMockito.doReturn(getFixture(deletesResponseFile))
                    .when(mockTransportProvider)
                    .get(modelUrlPathToken + "/actions", contentType);
    }

    @Test
    public void testDeleteGoodCase() throws Exception {
        // mock out API calls
        recordActionsFetchMockModels();
        recordActionsFetchMockDeletes();
        recordActionsFetchMockItems("files", filesResponseFile);
        recordActionsRunServerTask(deleteUrlPathToken);
        recordActionsTaskResultSuccess();

        AnaplanDeleteOperation deleteOp = new AnaplanDeleteOperation(
                mockAnaplanConnection);
        String result = deleteOp.runDeleteAction(workspaceId, modelId, deleteId);
        assertEquals("[deleteId] completed successfully!\n\n", result);
    }
}
