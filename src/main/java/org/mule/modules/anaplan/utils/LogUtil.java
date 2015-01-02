package org.mule.modules.anaplan.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

//import com.boomi.anaplan.connector.AnaplanConnection;
//import com.boomi.connector.api.OperationResponse;
//import com.boomi.connector.api.TrackedData;

/**
 * Log output to Boomi has 3 main routes:
 * <ul>
 * <li>Atom server log: Internal log for debug and reporting. Global or
 * class-based loggers output to here.
 * <li>Boomi process log: The main user-facing log. {@link OperationResponse}
 * loggers output to here.
 * <li>Boomi per-shape log: Component-specific user-facing logs.
 * {@link TrackedData} loggers output here.
 * </ul>
 * 
 * Messages logged at INFO and above should describe normal or error connector
 * operation in a user-friendly way if at all possible. All messages written to
 * the user-visible logs are mirrored to the server logs.
 * 
 * Server log level is set to DEBUG, there doesn't seem to be a way to configure
 * this through Boomi externally.
 * 
 * Processed customer data is written to internal logs at TRACE level only,
 * which is not displayed by default.
 */
public final class LogUtil {
	private LogUtil() {
		// static-only
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
	public static final Level USER_WARNING = Level.WARNING;

	/**
	 * User-presentable log message about an error that is preventing completion
	 * of the operation.
	 */
	public static final Level USER_ERROR = Level.SEVERE;

	/**
	 * Internal message with debug info.
	 */
	public static final Level INTERNAL_DEBUG = Level.FINE;

	/**
	 * Internal message tracing detailed operation execution.
	 * 
	 * Matches level used by {@link Logger#entering(String, String)} and
	 * {@link Logger#exiting(String, String)};
	 */
	public static final Level INTERNAL_TRACE = Level.FINER;

	/**
	 * Logger which writes to the Atom server container log.
	 */
	private static final Logger serverLog = Logger.getGlobal();
	static {
		serverLog.setLevel(INTERNAL_DEBUG);
	}

	public static void debug(String logContext, String msg) {
		serverLog.log(INTERNAL_DEBUG, logContext + " " + msg);
	}

	public static void trace(String logContext, String msg) {
		if (serverLog.isLoggable(INTERNAL_TRACE)) {
			serverLog.log(INTERNAL_TRACE, logContext + " " + msg);
		}
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
//	public static class ProcessLog extends UserLog {
//		public ProcessLog(OperationResponse response,
//				AnaplanConnection connection) {
//			super(response.getLogger(), connection.getLogContext());
//		}
//	}

	/**
	 * Write to the per-shape log regarding a particular data input.
	 */
//	public static class ShapeDataLog extends UserLog {
//		public ShapeDataLog(TrackedData input, AnaplanConnection connection) {
//			super(input.getLogger(), connection.getLogContext());
//		}
//	}
}
