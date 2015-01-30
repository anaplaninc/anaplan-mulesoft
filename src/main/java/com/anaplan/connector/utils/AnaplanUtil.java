/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package com.anaplan.connector.utils;

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
