/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

import java.net.http.HttpClient;

public class TestDependencies {

    private final HttpClient httpClient;
    private final Application application;

    public static final String[] baseHeaders = new String[]{"content-type", "application/json"};
    public static final String hCaptchaCookieName = "hCaptchaToken";
    public static final String sessionTokenCookieName = "sessionToken";
    public static final String notLoggedInKey = "notLoggedIn";
    public static final String mainQueriesFileName = "mainQueries.graphql";

    public static boolean loggingInitialized = false;

    public TestDependencies(boolean enableRateLimit){
        if(!loggingInitialized){
            LoggerInitializer.initializeDevelopmentConsoleLogger();
        }
        application = new Application(new ApplicationConfiguration().setRateLimitEnabled(enableRateLimit));
        application.start();
        httpClient = HttpClient.newHttpClient();

    }

    public void close(){
        application.close();
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

}
