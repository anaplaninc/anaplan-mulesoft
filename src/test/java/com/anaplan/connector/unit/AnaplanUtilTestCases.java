package com.anaplan.connector.unit;


import com.anaplan.client.Task;
import com.anaplan.client.TaskStatus;
import com.anaplan.connector.utils.AnaplanUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
@PrepareForTest({
        Task.class,
        TaskStatus.class})
public class AnaplanUtilTestCases {

    private Task mockTask;
    private TaskStatus mockTaskStatus;

    @Before
    public void setUp() {
        mockTask = Mockito.mock(Task.class);
        mockTaskStatus = Mockito.mock(TaskStatus.class);
    }

    @After
    public void tearDown() {
        mockTask = null;
        mockTaskStatus = null;
    }

    @Test
    public void testRunServerTask() throws Exception {
        final int mockServerPingCountLimit = 4;
        PowerMockito.doReturn(mockTaskStatus).when(mockTask).getStatus();
        PowerMockito.doReturn(TaskStatus.State.IN_PROGRESS).when(mockTaskStatus)
                .getTaskState();
        Mockito.when(mockTaskStatus.getTaskState()).thenAnswer(new Answer() {
            private int count = 0;

            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (count++ < mockServerPingCountLimit)
                    return TaskStatus.State.IN_PROGRESS;
                return TaskStatus.State.COMPLETE;
            }
        });

        TaskStatus resultStatus = AnaplanUtil.runServerTask(mockTask);
        assertEquals(mockTaskStatus, resultStatus);
    }
}
