open module com.fjellsoftware.retaildemo {
    requires io.loppi;
    requires com.fjellsoftware.javafunctionalutils;
    requires com.fjellsoftware.bcryptclientsalt;

    requires com.zaxxer.hikari;
    requires com.github.benmanes.caffeine;
    requires com.fasterxml.jackson.databind;
    requires io.javalin;
    requires io.github.bucket4j.core;
    requires java.net.http;
    requires org.slf4j;
    requires org.jetbrains.annotations;
    requires java.sql;
    requires java.desktop;
    requires jetty.servlet.api;
    requires commons.cli;
    requires jul.to.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.apache.httpcomponents.core5.httpcore5;
}
