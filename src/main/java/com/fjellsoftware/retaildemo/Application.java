/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

import com.fjellsoftware.javafunctionalutils.either.Either;
import com.fjellsoftware.javafunctionalutils.either.Left;
import com.fjellsoftware.javafunctionalutils.either.Right;
import com.fjellsoftware.retaildemo.authorizationcommon.*;
import com.fjellsoftware.retaildemo.demo.DemoDatabaseResetter;
import com.fjellsoftware.retaildemo.demo.DemoGraphiQLLoader;
import com.fjellsoftware.retaildemo.graphqlexecutor.GraphQLExecutorCustomer;
import com.fjellsoftware.retaildemo.graphqlexecutor.GraphQLExecutorStaff;
import com.fjellsoftware.retaildemo.util.InetAddressUtils;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.*;
import io.javalin.http.staticfiles.Location;
import io.loppi.orm.PostgresExecutionException;
import io.loppi.graphql.GraphQLExecutionResult;
import io.loppi.graphql.GraphQLRequestException;
import io.loppi.graphql.schema.GraphQLErrorTypeCategory;
import com.fjellsoftware.javafunctionalutils.opt.None;
import com.fjellsoftware.javafunctionalutils.opt.Opt;
import com.fjellsoftware.javafunctionalutils.opt.Some;
import com.fjellsoftware.retaildemo.authorizationcommon.userinfo.CaptchaUser;
import com.fjellsoftware.retaildemo.authorizationcommon.userinfo.UnauthenticatedUser;
import com.fjellsoftware.retaildemo.authorizationcommon.userinfo.UserInfo;
import com.fjellsoftware.retaildemo.authorizationcommon.cookie.CookieModification;
import com.fjellsoftware.retaildemo.authorizationcommon.cookie.CookieToAdd;
import com.fjellsoftware.retaildemo.authorizationcommon.cookie.CookieToRemove;
import org.apache.hc.core5.http.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.nio.charset.UnsupportedCharsetException;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Application {
    private final Logger logger = LoggerFactory.getLogger(Application.class);
    private final GraphQLExecutorCustomer customerGraphQLExecutor;
    private final GraphQLExecutorStaff staffGraphQLExecutor;
    private final LoginSessionService loginSessionService;
    private final RateLimiter rateLimiter;
    private final HCaptchaCache captchaCache;

    public static final String SESSION_TOKEN_COOKIE_NAME = "sessionToken";

    private final Javalin server;
    private final ScheduledExecutorService scheduler;
    private final ApplicationDependencies applicationDependencies;
    private final ApplicationConfiguration applicationConfiguration;
    private final Metrics metrics;

    public Application(ApplicationConfiguration configuration) {
        logger.info("Starting Retail demo...");
        this.applicationConfiguration = configuration;
        this.applicationDependencies = new ApplicationDependencies(configuration);
        this.customerGraphQLExecutor = applicationDependencies.getGraphQLExecutorCustomer();
        this.staffGraphQLExecutor = applicationDependencies.getGraphQLExecutorStaff();
        this.loginSessionService = applicationDependencies.getLoginSessionAuthenticationService();
        DemoGraphiQLLoader graphiQLLoader = applicationDependencies.getGraphiQLLoader();
        this.rateLimiter = applicationDependencies.getRateLimiter();
        this.captchaCache = applicationDependencies.getCaptchaCache();
        this.metrics = applicationDependencies.getMetrics();

        Consumer<JavalinConfig> config = (javalinConfig) -> {
            javalinConfig.showJavalinBanner = false;
            javalinConfig.compression.gzipOnly();
            javalinConfig.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/retaildemolanding";
                staticFiles.location = Location.CLASSPATH;
            });
        };
        this.server = Javalin.create(config)
                .get("/customer_graphiql", graphiQLLoader::httpGetCustomerGraphiQL)
                .get("/staff_graphiql", graphiQLLoader::httpGetStaffGraphiQL)
                // Endpoint used by regular customers.
                .post("/api/v1/graphql", this::handleCustomerRequest)
                // Endpoint used by staff members.
                .post("/api/v1/staff/graphql", this::handleStaffRequest)
                .exception(Exception.class, this::handleException)
                .before(this::recordIncomingHttpMetrics)
                .after(this::recordOutgoingHttpMetrics);

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    void start(){
        server.start(applicationConfiguration.getPortNumber());
        boolean isProduction = applicationConfiguration.isProduction();
        if(isProduction) {
            scheduler.scheduleAtFixedRate(() -> {
                OffsetDateTime yearAgo = OffsetDateTime.now().minusYears(1);
                OffsetDateTime fourHoursAgo = OffsetDateTime.now().minusHours(4);
                try {
                    DemoDatabaseResetter.resetDatabase(applicationDependencies.getLoppiService(), yearAgo, fourHoursAgo);
                } catch (Exception e) {
                    logger.error("Failed to delete old data.", e);
                }
            }, 1, 1, TimeUnit.MINUTES);
        }
        scheduler.scheduleAtFixedRate(metrics::tryGatherAndStoreMetrics, 1, 1, TimeUnit.MINUTES);
        logger.info("Retail demo has successfully started in {} mode, version: {}.",
                isProduction ? "production" : "development", applicationConfiguration.getVersion());
        Thread shutdownHook = new Thread(() -> logger.info("Shutting down...\n"));
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    void close(){
        server.close();
        scheduler.shutdown();
        applicationDependencies.getDataSource().close();
    }

    private void handleException(Exception e, Context ctx){
        HandlerType method = ctx.method();
        if (method.equals(HandlerType.GET)) {
            ctx.html("Internal server error.");
        }
        else{
            ctx.contentType(ContentType.APPLICATION_JSON.getMimeType());
            ctx.result(GraphQLRequestException.createInternal().jsonResult());
        }
        logger.error("Unexpected error.", e);
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void recordIncomingHttpMetrics(Context ctx){
        Metrics.incrementHttpRequestIncoming();
    }
    private void recordOutgoingHttpMetrics(Context ctx){
        Metrics.incrementHttpRequestOutgoing();
        HttpStatus status = ctx.status();
        int statusCode = status.getCode();
        if(statusCode >= 200 && statusCode < 300){
            Metrics.incrementHttp200();
        }
        else if(statusCode >= 400 && statusCode < 500){
            Metrics.incrementHttp400();
        }
        else if(statusCode >= 500){
            Metrics.incrementHttp500();
        }
    }

    private void handleCustomerRequest(Context ctx){
        Either<UserInfo, GraphQLRequestException> userInfoOrError = processCommonApiHttpAndAuthentication(ctx);
        UserInfo userInfo = null;
        switch (userInfoOrError){
        case Right<UserInfo, GraphQLRequestException> right -> {
            ctx.result(right.value().jsonResult());
            return;
        }
        case Left<UserInfo, GraphQLRequestException> left -> userInfo = left.value();
        }
        String fullQuery = ctx.body();
        GraphQLExecutorCustomer.ExecutionResultAndCookies executionResultAndCookies =
                customerGraphQLExecutor.tryParseAndExecute(fullQuery, userInfo);
        GraphQLExecutionResult graphQLExecutionResult = executionResultAndCookies.executionResult();
        ctx.result(graphQLExecutionResult.jsonResult());
        if(graphQLExecutionResult instanceof GraphQLRequestException errorResult){
            handleGraphQLErrorCommon(ctx, errorResult);
        }
        else{
            ctx.status(HttpStatus.OK);
            for (CookieModification cookieModification : executionResultAndCookies.cookiesToModify()) {
                String cookiePath = "/";
                boolean cookieSecure = true;
                boolean cookieHttpOnly = true;
                int cookieVersion = 1;
                Cookie cookie = switch (cookieModification){
                case CookieToAdd toAdd -> new Cookie(toAdd.name(), toAdd.value(), cookiePath,
                        24 * 3600, cookieSecure, cookieVersion, cookieHttpOnly);
                // We could use ctx.removeCookie, but some applications used for development handles cookies
                // incorrectly, so we set httpOnly and secure flags as well
                case CookieToRemove toRemove -> new Cookie(toRemove.name(), "", cookiePath,
                        0, cookieSecure, cookieVersion, cookieHttpOnly);
                };
                cookie.setSameSite(SameSite.STRICT);
                ctx.cookie(cookie);
            }
        }
    }

    private void handleStaffRequest(Context ctx){
        Either<UserInfo, GraphQLRequestException> userInfoOrError = processCommonApiHttpAndAuthentication(ctx);
        switch (userInfoOrError){
        case Right<UserInfo, GraphQLRequestException> right -> ctx.result(right.value().jsonResult());
        case Left<UserInfo, GraphQLRequestException> left -> {
            UserInfo userInfo = left.value();
            String fullQuery = ctx.body();
            GraphQLExecutionResult graphQLExecutionResult = staffGraphQLExecutor.tryParseAndExecute(fullQuery,
                    userInfo);
            ctx.result(graphQLExecutionResult.jsonResult());
            if(graphQLExecutionResult instanceof GraphQLRequestException errorResult){
                handleGraphQLErrorCommon(ctx, errorResult);
            }
            else{
                ctx.status(HttpStatus.OK);
            }
        }
        }
    }

    private final Right<UserInfo, GraphQLRequestException> unsupportedMediaTypeExceptionWrapped =
            new Right<>(new GraphQLRequestException("Unsupported media type.", GraphQLErrorTypeCategory.APPLICATION_VALIDATION));
    private final Right<UserInfo, GraphQLRequestException> unsupportedCharsetExceptionWrapped =
            new Right<>(new GraphQLRequestException("Unsupported charset.", GraphQLErrorTypeCategory.APPLICATION_VALIDATION));
    private Either<UserInfo, GraphQLRequestException> processCommonApiHttpAndAuthentication(Context ctx){
        ctx.contentType(ContentType.APPLICATION_JSON.getMimeType());
        InetAddress remoteAddress = InetAddressUtils.extractRemoteFromContext(ctx);
        boolean couldConsume = rateLimiter.tryConsumeRegular(remoteAddress, 1);
        if(!couldConsume){
            ctx.status(HttpStatus.TOO_MANY_REQUESTS);
            return new Right<>(RateLimiter.getRateLimitException());
        }
        String requestContentType = ctx.contentType();
        if(requestContentType == null){
            ctx.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            return unsupportedMediaTypeExceptionWrapped;
        }
        ContentType contentType;
        try {
            contentType = ContentType.parse(requestContentType);
        } catch (UnsupportedCharsetException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return unsupportedCharsetExceptionWrapped;
        }
        if(!contentType.getMimeType().equals(ContentType.APPLICATION_JSON.getMimeType())){
            ctx.status(HttpStatus.BAD_REQUEST);
            return unsupportedMediaTypeExceptionWrapped;
        }

        String captchaCookieValueOrNull = ctx.cookie(HCaptchaVerifier.H_CAPTCHA_TOKEN_COOKIE_NAME);
        boolean captchaIsValid = captchaCookieValueOrNull != null && captchaCache.checkTokenPresent(captchaCookieValueOrNull);
        UserInfo userInfo;
        String sessionCookieOrNull = ctx.cookie(SESSION_TOKEN_COOKIE_NAME);
        if(sessionCookieOrNull == null){
            userInfo = new UnauthenticatedUser(remoteAddress);
        }
        else {
            UUID token = null;
            try {
                token = UUID.fromString(sessionCookieOrNull);
            } catch (IllegalArgumentException ignored) {}
            if(token == null){
                // Deal with bad applications not handling cookie deletion properly.
                userInfo = new UnauthenticatedUser(remoteAddress);
            }
            else{
                Opt<UserInfo> userInfoOpt;
                try {
                    userInfoOpt = loginSessionService.getUserInfoFromCacheOrFetch(token, remoteAddress);
                } catch (PostgresExecutionException e) {
                    logger.error("Internal server error.", e);
                    ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                    return new Right<>(GraphQLRequestException.createInternal(e));
                }
                userInfo = switch (userInfoOpt) {
                case Some<UserInfo> someUserInfo -> someUserInfo.value();
                case None<UserInfo> ignored -> {
                    ctx.removeCookie(SESSION_TOKEN_COOKIE_NAME);
                    yield new UnauthenticatedUser(remoteAddress);
                }
                };
            }
        }

        if(userInfo instanceof UnauthenticatedUser && captchaIsValid){
            userInfo = new CaptchaUser(userInfo.remoteAddress(), captchaCookieValueOrNull);
        }

        return new Left<>(userInfo);
    }

    private void handleGraphQLErrorCommon(Context ctx, GraphQLRequestException errorResult){
        GraphQLErrorTypeCategory errorTypeCategory = errorResult.getErrorTypeCategory();
        switch (errorTypeCategory){
        case INTERNAL -> {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            logger.error("Internal server error.", errorResult);
        }
        // The semantic meaning of HttpStatus.UNAUTHORIZED (401) is essentially "Not logged in".
        case AUTHENTICATION -> ctx.status(HttpStatus.UNAUTHORIZED);
        case AUTHORIZATION -> ctx.status(HttpStatus.FORBIDDEN);
        default -> {
            if(errorResult instanceof GraphQLRateLimitException){
                ctx.status(HttpStatus.TOO_MANY_REQUESTS);
            }
            else {
                ctx.status(HttpStatus.BAD_REQUEST);
            }
        }
        }
    }

}
