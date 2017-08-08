/**
 * Copyright 2015 Anaplan Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License.md file for the specific language governing permissions and
 * limitations under the License.
 */

package com.anaplan.connector.utils;

import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.Task;
import com.anaplan.client.TaskStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Utilities here handle communication with the Anaplan API
 *
 * @author spondonsaha
 */
public class AnaplanUtil {

    private static final Logger logger = LogManager.getLogger(AnaplanUtil.class.getName());
    public static final int CHUNKSIZE = 2048;

    private AnaplanUtil() {
        // static-only
    }

    /**
     * Executes an Anaplan task and polls the status until its complete.
     *
     * @param task Server task object to run.
     * @return The status message from the running the task.
     * @throws AnaplanAPIException API exception thrown whenever server task
     *      fails.
     */
    public static TaskStatus runServerTask(Task task)
            throws AnaplanAPIException {
        TaskStatus status = task.getStatus();
        logger.info("TASK STATUS: {}", status.getTaskState());
        while (status.getTaskState() != TaskStatus.State.COMPLETE
                && status.getTaskState() != TaskStatus.State.CANCELLED) {

            // if busy, nap and check again after 1 second
            try {
                Thread.sleep(1000);
                logger.info("Running Task = {}", task.getStatus().getProgress());
            } catch (InterruptedException e) {
                logger.error("Task interrupted!\n{}", e.getMessage());
            }
            status = task.getStatus();
        }

        return status;
    }
}
