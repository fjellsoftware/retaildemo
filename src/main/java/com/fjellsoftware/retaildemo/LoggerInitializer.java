/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.status.Status;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

public class LoggerInitializer {

    private static final String loggingPattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %highlight(%-5level) %cyan(%logger{15}) - %msg %n";
    public static Logger initializeDevelopmentConsoleLogger(){
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setPattern(loggingPattern);
        patternLayoutEncoder.setContext(loggerContext);
        patternLayoutEncoder.start();

        ConsoleAppender consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setEncoder(patternLayoutEncoder);
        consoleAppender.start();
        return detachAndSetRootLogger(consoleAppender);
    }

    public static Logger initializeProductionAsyncFileLogger(String fileDirectory){
        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
        LoggerContext loggerContext = (LoggerContext) iLoggerFactory;
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setPattern(loggingPattern);
        patternLayoutEncoder.setContext(loggerContext);
        patternLayoutEncoder.start();

        FileAppender fileAppender = new FileAppender();
        fileAppender.setContext(loggerContext);
        fileAppender.setEncoder(patternLayoutEncoder);
        fileAppender.setAppend(true);
        fileAppender.setFile(Path.of(fileDirectory, "logfile.log").toString());
        fileAppender.start();

        boolean wasStarted = fileAppender.isStarted();
        if(!wasStarted){
            java.util.logging.Logger julLogger = java.util.logging.Logger.getGlobal();
            julLogger.log(java.util.logging.Level.SEVERE, "Failed to start loppi server, could not initialize file " +
                    "logger.");
            List<Status> statuses = fileAppender.getContext().getStatusManager().getCopyOfStatusList();
            for (Status status : statuses) {
                if(status.getLevel() == Status.ERROR) {
                    julLogger.log(java.util.logging.Level.SEVERE, status.getMessage(), status.getThrowable());
                }
            }
            System.exit(0);
        }
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(loggerContext);
        asyncAppender.setDiscardingThreshold(0);
        asyncAppender.addAppender(fileAppender);
        asyncAppender.start();
        return detachAndSetRootLogger(asyncAppender);
    }
    private static Logger detachAndSetRootLogger(Appender appender){
        Logger root = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.detachAndStopAllAppenders();
        root.setLevel(Level.INFO);
        root.setAdditive(false);
        root.setLevel(Level.INFO);
        root.addAppender(appender);
        return root;
    }
}
