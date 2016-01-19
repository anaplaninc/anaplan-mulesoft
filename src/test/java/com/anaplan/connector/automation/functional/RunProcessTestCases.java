package com.anaplan.connector.automation.functional;

import static org.junit.Assert.*;

import com.anaplan.connector.AnaplanConnector;
import com.anaplan.connector.exceptions.AnaplanConnectionException;
import com.anaplan.connector.exceptions.AnaplanOperationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

public class RunProcessTestCases extends AbstractTestCase<AnaplanConnector> {

    public RunProcessTestCases() {
        super(AnaplanConnector.class);
    }

    @Before
    public void setup() {
        // TODO
    }

    @After
    public void tearDown() {
        // TODO
    }

    @Test
    public void verify() throws AnaplanConnectionException,
            AnaplanOperationException {
        java.lang.String expected = null;
        java.lang.String workspaceId = null;
        java.lang.String modelId = null;
        java.lang.String processId = null;
        assertEquals(
                getConnector().runProcess(workspaceId, modelId, processId),
                expected);
    }

}