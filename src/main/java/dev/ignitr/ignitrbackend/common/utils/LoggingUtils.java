package dev.ignitr.ignitrbackend.common.utils;

import org.bson.types.ObjectId;
import org.slf4j.Logger;

import static dev.ignitr.ignitrbackend.common.utils.StringUtils.isNotNullOrEmpty;

public class LoggingUtils {

    private LoggingUtils() {}

    private static String prefix(String operation, String resourceId) {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append("operation: ");
        if(isNotNullOrEmpty(operation)) {
            builder.append(operation);
        } else {
            builder.append("unknown");
        }
        builder. append(", resourceId: ");
        if (isNotNullOrEmpty(resourceId)) {
            builder.append(resourceId);
        } else {
            builder.append("unknown");
        }
        builder.append("] ");
        return builder.toString();
    }

    private static String prefix(String operation, ObjectId resourceId) {
        StringBuilder builder = new StringBuilder();
        String stringId = resourceId != null ? resourceId.toHexString() : null;
        builder.append("[").append("operation: ");
        if(isNotNullOrEmpty(operation)) {
            builder.append(operation);
        } else {
            builder.append("unknown");
        }
        if (isNotNullOrEmpty(stringId)) {
            builder. append(", resourceId: ");
            builder.append(resourceId);
        }
        builder.append("] ");
        return builder.toString();
    }



    private static String build(String operation, String resourceId, String message) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix(operation, resourceId));
        if (isNotNullOrEmpty(message)) {
            builder.append(message);
        } else {
            builder.append("No additional message provided.");
        }
        return builder.toString();
    }

    private static String build(String operation, ObjectId resourceId, String message) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix(operation, resourceId));
        if (isNotNullOrEmpty(message)) {
            builder.append(message);
        } else {
            builder.append("No additional message provided.");
        }
        return builder.toString();
    }

    public static void debug(Logger logger, String operation, String resourceId, String message, Object... args) {
        if(logger.isDebugEnabled()) {
            logger.debug(build(operation, resourceId, message), args);
        }
    }

    public static void debug(Logger logger, String operation, ObjectId resourceId, String message, Object... args) {
        if(logger.isDebugEnabled()) {
            logger.debug(build(operation, resourceId, message), args);
        }
    }

    public static void info(Logger logger, String operation, String resourceId, String message, Object... args) {
        if(logger.isInfoEnabled()) {
            logger.info(build(operation, resourceId, message), args);
        }
    }

    public static void info(Logger logger, String operation, ObjectId resourceId, String message, Object... args) {
        if(logger.isInfoEnabled()) {
            logger.info(build(operation, resourceId, message), args);
        }
    }

    public static void warn(Logger logger, String operation, String resourceId, String message, Object... args) {
        if(logger.isWarnEnabled()) {
            logger.warn(build(operation, resourceId, message), args);
        }
    }

    public static void warn(Logger logger, String operation, ObjectId resourceId, String message, Object... args) {
        if(logger.isWarnEnabled()) {
            logger.warn(build(operation, resourceId, message), args);
        }
    }

    public static void warn(Logger logger, String operation, String resourceId, String message, Throwable throwable) {
        if(logger.isWarnEnabled()) {
            logger.warn(build(operation, resourceId, message), throwable);
        }
    }

    public static void warn(Logger logger, String operation, ObjectId resourceId, String message, Throwable throwable) {
        if(logger.isWarnEnabled()) {
            logger.warn(build(operation, resourceId, message), throwable);
        }
    }

    public static void error(Logger logger, String operation, String resourceId, Throwable throwable, String message, Object... args) {
        if(logger.isErrorEnabled()) {
            logger.error(build(operation, resourceId, message), args, throwable);
        }
    }

    public static void error(Logger logger, String operation, ObjectId resourceId, Throwable throwable, String message, Object... args) {
        if(logger.isErrorEnabled()) {
            logger.error(build(operation, resourceId, message), args, throwable);
        }
    }
}
