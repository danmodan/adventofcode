package com.danmodan.adventofcode.common.config;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class LoggerConfig {

    private final Level logLevel;

    public LoggerConfig(@Value("${log.level:#{T(java.util.logging.Level).SEVERE}}") Level logLevel) {
        this.logLevel = logLevel;
    }

    @PostConstruct
    public void createRootLogger() {

        Logger rootLogger = Logger.getLogger("com.danmodan.adventofcode");
        rootLogger.setLevel(logLevel);
        rootLogger.setUseParentHandlers(false);
        rootLogger.addHandler(getConsoleHandler());
    }

    private Handler getConsoleHandler() {

        Handler handler = new ConsoleHandler();
        handler.setFormatter(getFormatter());
        handler.setLevel(logLevel);
        return handler;
    }

    private Formatter getFormatter() {

        return new Formatter() {
            @Override
            public String format(LogRecord lr) {

                String threadName = Thread.currentThread().getName();
                Level level = lr.getLevel();
                String message = lr.getMessage();
                Throwable thrown = lr.getThrown();

                if (thrown != null) {

                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    thrown.printStackTrace(pw);

                    return String.format("[%s][%s] %s%n%s%n%s%n", threadName, level, message,
                            thrown.getMessage(), sw.toString());
                }

                return String.format("[%s][%s] %s%n", threadName, level, message);
            }
        };
    }
}
