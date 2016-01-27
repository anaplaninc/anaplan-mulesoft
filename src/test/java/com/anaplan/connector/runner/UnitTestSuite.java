package com.anaplan.connector.runner;

import com.anaplan.connector.unit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ConnectionUnitTestCases.class,
        ImportOperationUnitTestCases.class,
        ExportOperationUnitTestCases.class,
        ProcessOperationUnitTestCases.class,
        DeleteOperationUnitTestCases.class})
public class UnitTestSuite {

}
