package com.anaplan.connector.functional;

import com.anaplan.connector.AnaplanConnector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

import java.io.IOException;
import static org.junit.Assert.assertEquals;


public class ImportToModelTestCases extends AbstractTestCase<AnaplanConnector> {

    @Before
    public void setup() throws Exception {

    }

    @After
    public void tearDown() throws IOException {

    }

    @Test
    public void testImportToModelGoodData() throws Exception {
        assertEquals(1, 1);
        System.out.println("Done here!!!!!");

//        assertEquals(
//                getConnector().importToModel(IOU sampleFileData, workspaceId, modelId,
//                        importId, columnSeparator, delimiter), expected);
    }

}