
package org.mule.modules.automation.testcases;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class UpdateTestCases
    extends anaplanTestParent
{


    @Before
    public void setup() {
        //TODO: Add setup required to run test or remove method
        initializeTestRunMessage("updateTestData");
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
    public void testUpdate()
        throws Exception
    {
        Object result = runFlowAndGetPayload("update");
        throw new RuntimeException("NOT IMPLEMENTED METHOD");
    }

}
