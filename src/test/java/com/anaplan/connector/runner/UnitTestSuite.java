package com.anaplan.connector.runner;

import com.anaplan.connector.unit.ConnectionUnitTestCases;
import com.anaplan.connector.unit.ExportOperationUnitTestCases;
import com.anaplan.connector.unit.ImportOperationUnitTestCases;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ConnectionUnitTestCases.class,
        ImportOperationUnitTestCases.class,
        ExportOperationUnitTestCases.class})
public class UnitTestSuite {

}
