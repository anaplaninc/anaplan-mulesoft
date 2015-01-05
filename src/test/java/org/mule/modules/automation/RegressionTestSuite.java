
package org.mule.modules.automation;

import org.junit.runner.RunWith;
import org.mule.modules.automation.testcases.CreateTestCases;
import org.mule.modules.automation.testcases.DeleteTestCases;
import org.mule.modules.automation.testcases.ReadTestCases;
import org.mule.modules.automation.testcases.RegressionTests;
import org.mule.modules.automation.testcases.UpdateTestCases;

@RunWith(org.junit.experimental.categories.Categories.class)
@org.junit.experimental.categories.Categories.IncludeCategory(RegressionTests.class)
@org.junit.runners.Suite.SuiteClasses({
    CreateTestCases.class,
    ReadTestCases.class,
    UpdateTestCases.class,
    DeleteTestCases.class
})
public class RegressionTestSuite {


}
