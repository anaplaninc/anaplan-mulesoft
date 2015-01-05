
package org.mule.modules.automation.testcases;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class DeleteTestCases
    extends anaplanTestParent
{


    @Before
    public void setup() {
        //TODO: Add setup required to run test or remove method
        initializeTestRunMessage("deleteTestData");
    }

    @After
    public void tearDown() {
        //TODO: Add code to reset sandbox state to the one before the test was run or remove
    }

    @Category({
        RegressionTests.class,
        SmokeTests.class
    })
    @Test
    public void testDelete()
        throws Exception
    {
        Object result = runFlowAndGetPayload("delete");
        throw new RuntimeException("NOT IMPLEMENTED METHOD");
    }

}
