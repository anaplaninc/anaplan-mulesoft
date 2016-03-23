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

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Utilities here handle communication with the Anaplan API
 *
 * @author spondonsaha
 */
public class AnaplanUtil {

    private static Logger logger = LogManager.getLogger(AnaplanUtil.class.getName());
    public static final int CHUNKSIZE = 2048;
    public static final Pattern crPattern = Pattern.compile("(.+\r\n)|(.+\r)|(.+\n)");

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

    /**
     * Overloading for stringChunkReader(String, Integer).
     *
     * @param data
     *            String data.
     * @return
     */
    public static Iterator<String> stringChunkReader(final String data) {
        return stringChunkReader(data, CHUNKSIZE);
    }

    /**
     * Data splitter to be used in association with arrayToBase64(). Splits up
     * the data by provided chunkSize value and returns an iterator to iterate
     * over each chunk.
     *
     * @param data
     *            Data string, base64 friendly.
     * @param chunkSize
     *            Chunk size limit, defaults to 2048 characters.
     * @return Iterator to iterate over each data-chunk.
     */
    public static Iterator<String> stringChunkReader(final String data,
                                                    final int chunkSize) {

        return new Iterator<String>() {

            int index = 0;
            String dataChunk;

            @Override
            public boolean hasNext() {
                return index < data.length();
            }

            @Override
            public String next() {
                if (hasNext()) {
                    dataChunk = data.substring(index, Math.min(index + chunkSize,
                            data.length()));
                    index += chunkSize;
                    return dataChunk;
                }
                return null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(
                        "Iterator not fail-safe!");
            }
        };
    }


    // TODO: Gets an almost accurate row-count, using a regex.
    // Needs to be ultimately replaced with count returned from server.
    /**
     * Fetches the number of carriage-returns from each char-array, which can be
     * cast as string.
     * @param charArray Byte array of the string that needs to be introspected.
     * @return number of carriage returns found in provided byte-array.
     */
    public static int getCarriageReturnCount(byte[] charArray) {

        Matcher m = crPattern.matcher(new String(charArray));
        int lines = 0;
        while (m.find()) {
            lines++;
        }
        return lines;
    }
}
