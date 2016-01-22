package com.anaplan.connector.functional;

import static org.junit.Assert.*;

import com.anaplan.connector.AnaplanConnector;
import com.anaplan.connector.exceptions.AnaplanConnectionException;
import com.anaplan.connector.exceptions.AnaplanOperationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

public class ExportFromModelTestCases extends AbstractTestCase<AnaplanConnector> {

    public ExportFromModelTestCases() {
        super();
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
        java.lang.String exportId = null;
        assertEquals(
                getConnector().exportFromModel(workspaceId, modelId, exportId),
                expected);
    }

}