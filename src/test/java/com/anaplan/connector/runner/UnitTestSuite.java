package com.anaplan.connector.runner;

import com.anaplan.connector.unit.ConnectionUnitTestCases;
import com.anaplan.connector.unit.ImportOperationTestCases;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ConnectionUnitTestCases.class,
        ImportOperationTestCases.class})
public class UnitTestSuite {

}
