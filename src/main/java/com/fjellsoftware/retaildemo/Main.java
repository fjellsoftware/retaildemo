/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args){
        java.util.logging.Logger julLogger = java.util.logging.Logger.getGlobal();
        try {
            Options options = new Options();

            Option helpOption = Option.builder("h")
                    .longOpt("help")
                    .desc("Print this message.")
                    .build();
            options.addOption(helpOption);

            Option versionOption = Option.builder("v")
                    .longOpt("version")
                    .desc("Print the version of this application.")
                    .build();
            options.addOption(versionOption);

            Option modeOption = Option.builder("m")
                    .longOpt("mode")
                    .argName("dev/prod")
                    .required(false)
                    .hasArg()
                    .desc("Mode the application should use. Valid values are dev and prod.")
                    .build();
            options.addOption(modeOption);

            Option portOption = Option.builder("p")
                    .longOpt("port")
                    .argName("port-number")
                    .hasArg()
                    .required(false)
                    .desc("Port number the server will listen to. Default 8080.")
                    .build();
            options.addOption(portOption);

            Option uriOption = Option.builder("U")
                    .longOpt("uri")
                    .argName("uri")
                    .hasArg()
                    .required(false)
                    .desc("URI, for example \"https://mycompany.com\" or \"http://localhost:8080\" (default).")
                    .build();
            options.addOption(uriOption);

            Option logFileDirectoryNameOption = Option.builder("l")
                    .longOpt("log-dir")
                    .argName("logging directory")
                    .hasArg()
                    .required(false)
                    .desc("Directory path for where the logs should go. Required if running in production mode, " +
                            "otherwise will trigger error.")
                    .build();
            options.addOption(logFileDirectoryNameOption);

            Option credentialsDirectoryOption = Option.builder("c")
                    .longOpt("credentials")
                    .argName("credentials directory")
                    .hasArg()
                    .required(false)
                    .desc("Directory path for where the program will look for credential files. The default value is " +
                            "the user's home directory.")
                    .build();
            options.addOption(credentialsDirectoryOption);

            DefaultParser parser = new DefaultParser();
            CommandLine commandLine = null;
            try {
                commandLine = parser.parse(options, args);
            } catch (ParseException e) {
                julLogger.log(java.util.logging.Level.SEVERE, e.getMessage());
                System.exit(0);
            }
            if (commandLine.hasOption(helpOption)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("Retail demo", options);
                System.exit(0);
            }
            final Properties properties = new Properties();
            try {
                properties.load(Application.class.getClassLoader().getResourceAsStream("project.properties"));
            } catch (IOException e) {
                throw new ApplicationInternalException("Failed to load project properties.", e);
            }
            String version = properties.getProperty("version");
            if (commandLine.hasOption(versionOption)) {
                julLogger.log(Level.INFO, "Version: " + version);
                System.exit(0);
            }

            String modeValueNullable = commandLine.getOptionValue(modeOption);
            boolean isProduction = false;
            if(modeValueNullable == null){
                julLogger.log(java.util.logging.Level.SEVERE, "Option \"mode\" must be set.");
                System.exit(0);
            }
            if (modeValueNullable.equals("dev")) {

            }
            else if (modeValueNullable.equals("prod")) {
                isProduction = true;
            }
            else {
                julLogger.log(java.util.logging.Level.SEVERE, "Invalid value {0} for option \"mode\". Must be \"dev\" or \"prod\".", modeValueNullable);
                System.exit(0);
            }

            String logDirValueNullable = commandLine.getOptionValue(logFileDirectoryNameOption);

            Logger rootLogger;
            if (isProduction) {
                if (logDirValueNullable == null) {
                    julLogger.log(java.util.logging.Level.SEVERE, "Must provide a logging directory when running in production mode.");
                    System.exit(0);
                }
                julLogger.log(java.util.logging.Level.INFO, "File logger will be initialized, then " +
                        "logs will go to log files in {0}.", logDirValueNullable);
                rootLogger = LoggerInitializer.initializeProductionAsyncFileLogger(logDirValueNullable);
            } else {
                rootLogger = LoggerInitializer.initializeDevelopmentConsoleLogger();
            }
            activateJulToSLF4JBridge();
            int port = 8080;
            String userProvidedPortNullable = commandLine.getOptionValue(portOption);
            if (userProvidedPortNullable != null) {
                try {
                    port = Integer.parseInt(userProvidedPortNullable);
                } catch (NumberFormatException e) {
                    rootLogger.error("Invalid value {} for option \"port\". Value must be number.", userProvidedPortNullable);
                    System.exit(0);
                }
            }

            String userProvidedUriNullable = commandLine.getOptionValue(uriOption);
            String uri = userProvidedUriNullable == null ? "http://localhost:8080" : userProvidedUriNullable;
            ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration()
                    .setVersion(version)
                    .setIsProduction(isProduction)
                    .setRateLimitEnabled(true)
                    .setPortNumber(port)
                    .setURIName(uri);
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            Application application = new Application(applicationConfiguration);
            application.start();
        } catch (Exception e){
            julLogger.log(java.util.logging.Level.SEVERE, "{0}", e);
            throw e;
        }
    }

    private static void activateJulToSLF4JBridge(){
        // Removes existing handlers attached to j.u.l root logger. These makes j.u.l logs go to SLF4J implementation.
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
}
