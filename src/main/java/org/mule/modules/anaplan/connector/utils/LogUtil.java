/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
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
