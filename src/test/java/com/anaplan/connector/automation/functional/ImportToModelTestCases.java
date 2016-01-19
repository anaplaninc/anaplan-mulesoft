package com.anaplan.connector.automation.functional;

import static org.junit.Assert.*;

import com.anaplan.connector.AnaplanConnector;
import com.anaplan.connector.exceptions.AnaplanConnectionException;
import com.anaplan.connector.exceptions.AnaplanOperationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

public class ImportToModelTestCases extends AbstractTestCase<AnaplanConnector> {

    public ImportToModelTestCases() {
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
        java.lang.String data = null;
        java.lang.String workspaceId = null;
        java.lang.String modelId = null;
        java.lang.String importId = null;
        java.lang.String columnSeparator = null;
        java.lang.String delimiter = null;
        assertEquals(
                getConnector().importToModel(data, workspaceId, modelId,
                        importId, columnSeparator, delimiter), expected);
    }

}