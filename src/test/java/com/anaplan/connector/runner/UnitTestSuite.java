package com.anaplan.connector.runner;

import com.anaplan.connector.unit.AnaplanUtilTestCases;
import com.anaplan.connector.unit.ConnectionUnitTestCases;
import com.anaplan.connector.unit.DeleteOperationUnitTestCases;
import com.anaplan.connector.unit.ExportOperationUnitTestCases;
import com.anaplan.connector.unit.ImportOperationUnitTestCases;
import com.anaplan.connector.unit.ProcessOperationUnitTestCases;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		AnaplanUtilTestCases.class,
        ConnectionUnitTestCases.class,
        ImportOperationUnitTestCases.class,
        ExportOperationUnitTestCases.class,
        ProcessOperationUnitTestCases.class,
        DeleteOperationUnitTestCases.class})
public class UnitTestSuite {

}
