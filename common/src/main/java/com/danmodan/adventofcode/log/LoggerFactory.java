package com.danmodan.adventofcode.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggerFactory {

    private static final Handler HANDLER;
    private static final Formatter FORMATTER;
    private static final Collection<Logger> ALL_LOGGERS = new HashSet<>();

    static {

        FORMATTER = new Formatter() {
            @Override
            public String format(LogRecord lr) {
                ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                String name = threadMXBean.getThreadInfo(lr.getLongThreadID()).getThreadName();

                if (lr.getThrown() != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    lr.getThrown().printStackTrace(pw);
                    return String.format("[%s][%s]: %s%n%s", lr.getLevel(), name, lr.getMessage(), sw.toString());
                }

                return String.format("[%s][%s]: %s%n", lr.getLevel(), name, lr.getMessage());
            }
        };

        HANDLER = new ConsoleHandler();
        HANDLER.setFormatter(FORMATTER);
    }

    private LoggerFactory() {
    }

    public static Logger getLogger(Class clazz) {

        Logger logger = Logger.getLogger(clazz.getName());
        logger.setUseParentHandlers(false);
        logger.addHandler(HANDLER);
        ALL_LOGGERS.add(logger);
        return logger;
    }

    public static void setRootLoggerLevel(Level rootLoggerLevel) {

        for (Logger logger : ALL_LOGGERS) {
            logger.setLevel(rootLoggerLevel);
        }
    }
}
