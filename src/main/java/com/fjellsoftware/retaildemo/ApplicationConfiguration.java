/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

public class ApplicationConfiguration {
    private String version = "UNKNOWN";
    private String uri = "http://localhost:8080";
    private int portNumber = 8080;
    private boolean rateLimitEnabled = true;
    private boolean isProduction = false;
    private String credentialsDirectory = System.getProperty("user.home");

    public String getVersion() {
        return version;
    }

    public ApplicationConfiguration setVersion(String version) {
        this.version = version;
        return this;
    }

    public boolean isRateLimitEnabled() {
        return rateLimitEnabled;
    }

    public ApplicationConfiguration setRateLimitEnabled(boolean rateLimitEnabled) {
        this.rateLimitEnabled = rateLimitEnabled;
        return this;
    }

    public boolean isProduction() {
        return isProduction;
    }

    public ApplicationConfiguration setIsProduction(boolean isProduction){
        this.isProduction = isProduction;
        return this;
    }

    public String getURI() {
        return uri;
    }

    public ApplicationConfiguration setURIName(String hostName) {
        this.uri = hostName;
        return this;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public ApplicationConfiguration setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    public String getCredentialsDirectory() {
        return credentialsDirectory;
    }

    public void setCredentialsDirectory(String credentialsDirectory) {
        this.credentialsDirectory = credentialsDirectory;
    }
}
