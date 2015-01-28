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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mule.modules.anaplan.connector.AnaplanConnection;


/**
 * Simple log-wrapper around Log4j with debug, trace, status, warning and error
 * static log methods.
 */
public final class LogUtil {

	private LogUtil() {
		// static only
	}

	/**
	 * User-presentable log message about the status of an operation which is
	 * proceeding normally.
	 */
	public static final Level USER_STATUS = Level.INFO;

	/**
	 * User-presentable log message about problems encountered by an operation
	 * which can be handled.
	 */
	public static final Level USER_WARNING = Level.WARN;

	/**
	 * User-presentable log message about an error that is preventing completion
	 * of the operation.
	 */
	public static final Level USER_ERROR = Level.FATAL;

	/**
	 * Internal message with debug info.
	 */
	public static final Level INTERNAL_DEBUG = Level.DEBUG;

	private static Logger serverLog = LogManager.getLogger();

	/**
	 * Internal message tracing detailed operation execution.
	 *
	 * Matches level used by {@link Logger#entering(String, String)} and
	 * {@link Logger#exiting(String, String)};
	 */
	public static final Level INTERNAL_TRACE = Level.TRACE;

	public static void debug(String logContext, String msg) {
		serverLog.log(INTERNAL_DEBUG, logContext + " " + msg);
	}

	public static void trace(String logContext, String msg) {
		serverLog.log(INTERNAL_TRACE, logContext + " " + msg);
	}

	public static void status(String logContext, String msg) {
		serverLog.log(USER_STATUS, logContext + " " + msg);
	}

	public static void warning(String logContext, String msg) {
		serverLog.log(USER_WARNING, logContext + " " + msg);
	}

	public static void error(String logContext, String msg) {
		serverLog.log(USER_ERROR, logContext + " " + msg);
	}

	public static void error(String logContext, String msg, Throwable e) {
		serverLog.log(USER_ERROR, logContext + " " + msg, e);
	}

	/**
	 * Logging statuses available to user-facing messages.
	 *
	 * All user log messages are mirrored to the main atom server log.
	 */
	public static abstract class UserLog {
		private final Logger logger;
		private final String logContext;

		public UserLog(Logger logger, String logContext) {
			this.logger = logger;
			this.logContext = logContext;
		}

		public void status(String msg) {
			logger.log(USER_STATUS, msg);
			LogUtil.status(mirrorPrefix() + logContext, msg);
		}

		public void warning(String msg) {
			logger.log(USER_WARNING, msg);
			LogUtil.warning(mirrorPrefix() + logContext, msg);
		}

		public void error(String msg) {
			logger.log(USER_ERROR, msg);
			LogUtil.error(mirrorPrefix() + logContext, msg);
		}

		private String mirrorPrefix() {
			return "[" + getClass().getSimpleName() + "] ";
		}
	}

	/**
	 * Write to the main user-facing process log regarding a particular
	 * operation.
	 */
	public static class ProcessLog extends UserLog {
		public ProcessLog(AnaplanConnection connection) {
			super(serverLog, connection.getLogContext());
		}
	}
}
