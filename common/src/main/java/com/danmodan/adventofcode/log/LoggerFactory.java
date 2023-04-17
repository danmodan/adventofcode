package com.danmodan.adventofcode.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggerFactory {

    private static Handler handler;

    private LoggerFactory() {
    }

    public static Logger getLogger(Class clazz) {

        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String clazzName) {

        return getLogger(clazzName, Level.SEVERE);
    }

    public static Logger getLogger(String clazzName, Level logLevel) {

        Logger logger = Logger.getLogger(clazzName);
        logger.setLevel(logLevel);
        logger.setUseParentHandlers(false);
        logger.addHandler(getConsoleHandler());
        return logger;
    }

    private static Handler getConsoleHandler() {

        if (handler != null) {
            return handler;
        }

        synchronized (LoggerFactory.class) {

            if (handler == null) {
                Handler newHandler = new ConsoleHandler();
                newHandler.setFormatter(getFormatter());
                handler = newHandler;
            }

            return handler;
        }
    }

    private static Formatter getFormatter() {

        return new Formatter() {
            @Override
            public String format(LogRecord lr) {

                String threadName = Thread.currentThread().getName();

                if (lr.getThrown() != null) {

                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    lr.getThrown().printStackTrace(pw);

                    return String.format("[%s][%s] %s%n%s%n%s", lr.getLevel(), threadName, lr.getMessage(),
                            lr.getThrown().getMessage(), sw.toString());
                }

                return String.format("[%s][%s] %s", lr.getLevel(), threadName, lr.getMessage());
            }
        };
    }

    public static void setRootLoggerLevel(Level rootLoggerLevel) {

        // for (Logger logger : ALL_LOGGERS) {
        // logger.setLevel(rootLoggerLevel);
        // }
    }
}
