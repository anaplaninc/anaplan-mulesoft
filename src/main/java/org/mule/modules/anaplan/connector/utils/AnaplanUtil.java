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

package org.mule.modules.anaplan.connector.utils;

import com.anaplan.client.AnaplanAPIException;
import com.anaplan.client.Task;
import com.anaplan.client.TaskStatus;


/**
 * Utilities here handle communication with the Anaplan API
 *
 * @author spondonsaha
 */
public class AnaplanUtil {

	private AnaplanUtil() {
		// static-only
	}

	/**
	 * @param lines
	 * @return '\n'-delimited concat of lines, no trailing newline. Returns
	 *         empty string if lines is null.
	 */
	public static String squish(String... lines) {
		if (lines == null || lines.length == 0) {
			return "";
		}

		final StringBuilder sb = new StringBuilder();
		sb.append(lines[0]);

		for (int l = 1; l < lines.length; ++l) {
			sb.append("\n");
			sb.append(lines[l]);
		}

		return sb.toString();
	}

	/**
	 * Prints an array of strings as a string, delimited by "||". This is for
	 * debug logs only.
	 *
	 * @param toprint
	 * @return
	 */
	public static String debug_output(String[] toprint) {
		String sb = "";
		for (String s : toprint) {
			sb += s + "||";
		}
		if (sb.length() > 1) {
			return sb.substring(0, sb.length() - 1);
		} else {
			return "*";
		}
	}

	/**
	 * Executes a Anaplan import or export task.
	 *
	 * @param task
	 * @param logContext
	 * @return
	 * @throws AnaplanAPIException
	 */
	public static TaskStatus runServerTask(Task task, String logContext)
			throws AnaplanAPIException {
		TaskStatus status = task.getStatus();
		LogUtil.error(logContext, "TASK STATUS: " + status.getTaskState().toString());
		while (status.getTaskState() != TaskStatus.State.COMPLETE
				&& status.getTaskState() != TaskStatus.State.CANCELLED) {

			// if busy, nap and check again after
			try {
				Thread.sleep(1000);
				LogUtil.debug(logContext, "Running Task = "
						+ task.getStatus().getProgress());
			} catch (InterruptedException e) {
				LogUtil.error(logContext,
						"Task interrupted!\n" + e.getMessage());
			}
			status = task.getStatus();
		}

		return status;
	}
}
