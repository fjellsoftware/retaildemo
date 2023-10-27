/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.domain;

import com.fjellsoftware.retaildemo.Application;
import com.fjellsoftware.retaildemo.CoreDependencies;
import com.fjellsoftware.retaildemo.Metrics;
import com.fjellsoftware.retaildemo.authorizationcommon.HCaptchaVerifier;
import com.fjellsoftware.retaildemo.authorizationcommon.LoginSessionService;
import com.fjellsoftware.retaildemo.authorizationcommon.MutationFieldAndCookies;
import com.fjellsoftware.retaildemo.authorizationcommon.RateLimiter;
import com.fjellsoftware.retaildemo.autogenerated.graphql.customer.*;
import com.fjellsoftware.retaildemo.autogenerated.orm.*;
import io.loppi.includablevalues.UUIDValue;
import io.loppi.orm.*;
import io.loppi.graphql.*;
import io.loppi.graphql.schema.*;
import io.loppi.orm.metamodel.Attribute;
import io.loppi.orm.query.DatabaseQuery;
import com.fjellsoftware.javafunctionalutils.ImmutableList;
import com.fjellsoftware.retaildemo.authorizationcommon.userinfo.AuthenticatedUser;
import com.fjellsoftware.retaildemo.authorizationcommon.userinfo.CaptchaUser;
import com.fjellsoftware.retaildemo.authorizationcommon.userinfo.UserInfo;
import com.fjellsoftware.retaildemo.authorizationcommon.cookie.CookieModification;
import com.fjellsoftware.retaildemo.authorizationcommon.cookie.CookieToAdd;
import com.fjellsoftware.retaildemo.authorizationcommon.cookie.CookieToRemove;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LoginSessionDomain {

    private final LoppiServiceRetailDemo loppiService;
    private final LoginSessionService loginSessionService;
    private final RateLimiter rateLimiter;
    private final MessageDigest sha256Digest;
    private final CustomerGraphQLService customerGraphQLService;


    public LoginSessionDomain(
            LoppiServiceRetailDemo loppiService, LoginSessionService loginSessionService, RateLimiter rateLimiter,
            MessageDigest sha256Digest, CustomerGraphQLService customerGraphQLService) {
        this.loppiService = loppiService;
        this.loginSessionService = loginSessionService;
        this.rateLimiter = rateLimiter;
        this.sha256Digest = sha256Digest;
        this.customerGraphQLService = customerGraphQLService;
    }

    public static GraphQLFieldDefinition createLoginCustomMutationDefinition(){
        GraphQLFieldDefinition succeededField = GraphQLFieldDefinition.builder().setName("succeeded")
                .setNotNull(true).setType(GraphQLScalarTypeCategory.Boolean).build();
        GraphQLFieldDefinition tooManyAttemptsField = GraphQLFieldDefinition.builder().setName("tooManyAttempts")
                .setNotNull(true).setType(GraphQLScalarTypeCategory.Boolean).build();
        GraphQLObjectType loginResult = GraphQLObjectType.builder().setName("LoginResult")
                .setFields(List.of(succeededField, tooManyAttemptsField)).build();

        GraphQLArgumentDefinition usernameArgument = GraphQLArgumentDefinition.builder().setName("username")
                .setType(GraphQLScalarTypeCategory.String).setNotNull(true).build();
        GraphQLArgumentDefinition passwordBcryptArgument = GraphQLArgumentDefinition.builder().setName("passwordBcrypt")
                .setType(GraphQLScalarTypeCategory.String).setNotNull(true).build();
        return GraphQLFieldDefinition.builder().setName("login")
                .setDescription(String.format("Login to the service. Requires solved captcha, with token sent via " +
                        "cookie \"%s\". Also requires password to be hashed using bcrypt and the salt from " +
                        "the \"%s\" field. If login is successful the response will include a " +
                        "set-cookie for \"%s\". After four unsuccessful username/password combinations, " +
                        "you need to solve captcha again.", HCaptchaVerifier.H_CAPTCHA_TOKEN_COOKIE_NAME,
                        CoreDependencies.GET_BCRYPT_SALT_FIELD_NAME,
                        Application.SESSION_TOKEN_COOKIE_NAME))
                .setType(loginResult)
                .setNotNull(true)
                .setArguments(List.of(usernameArgument, passwordBcryptArgument))
                .build();
    }

    public static GraphQLFieldDefinition createSignOutCustomMutationDefinition(){
        return GraphQLFieldDefinition.builder().setName("signOut")
                .setDescription("Signs out of the service.")
                .setType(IgnoredResult.getGraphQLObjectType())
                .setNotNull(true).build();
    }

    public MutationFieldAndCookies handleSignOut(
            UserInfo userInfo, SignOutMutationField signOut)
            throws GraphQLRequestException {
        if(!(userInfo instanceof AuthenticatedUser authenticatedUser)){
            throw GraphQLRequestException.createNotLoggedIn();
        }
        LoginSessionToUpdate userSessionToUpdate = new LoginSessionToUpdate(authenticatedUser.sessionToken());
        userSessionToUpdate.setIsSignedOutIncludableOf(true);
        GraphQLExecutableMutationField parsedMutation =
                customerGraphQLService.createExecutableCustomMutationField(
                        List.of(userSessionToUpdate), new SignOutResult(true), signOut);
        List<CookieModification> cookieModifications = List.of(new CookieToRemove(Application.SESSION_TOKEN_COOKIE_NAME));
        loginSessionService.invalidateSession(authenticatedUser.sessionToken());
        return new MutationFieldAndCookies(parsedMutation, cookieModifications);
    }

    public MutationFieldAndCookies handleLogin(
            UserInfo userInfo, LoginMutationField loginMutationField)
            throws GraphQLRequestException {
        if(userInfo instanceof AuthenticatedUser){
            throw new GraphQLRequestException("User already logged in.", GraphQLErrorTypeCategory.OTHER);
        }
        if(!(userInfo instanceof CaptchaUser captchaUser)) {
            throw new GraphQLRequestException("Invalid captcha token. " +
                    "Please go back and solve the captcha.", GraphQLErrorTypeCategory.OTHER);
        }
        String captchaToken = captchaUser.captchaToken();
        boolean wasConsumed = rateLimiter.checkCanConsumeLoginGraphQL(captchaToken);
        if(!wasConsumed){
            Metrics.incrementLoginError();
            LoginResult tooManyAttemptsResult = new LoginResult(true, false);
            GraphQLExecutableMutationField executableMutationField = customerGraphQLService
                    .createExecutableCustomMutationField(List.of(), tooManyAttemptsResult, loginMutationField);
            return new MutationFieldAndCookies(executableMutationField, List.of());
        }
        DatabaseServiceJDBCRetailDemo databaseServiceJDBC = loppiService.getDatabaseServiceJDBC();
        MetaRetailDemo meta = loppiService.getMeta();
        UserAccountMeta userAccountMeta = meta.getUserAccountMeta();
        LoginMutationInput loginInput = loginMutationField.input();
        String usernameInput = loginInput.username();
        DatabaseQuery<UserAccountResult> query = databaseServiceJDBC.createQueryBuilder(UserAccountResult.class, 1)
                .includeScalar(userAccountMeta.userAccountId)
                .includeScalar(userAccountMeta.username)
                .includeScalar(userAccountMeta.hashedPassword)
                .includeScalar(userAccountMeta.role)
                .where().eq(userAccountMeta.username, usernameInput).end()
                .build();
        QueryExecutionResult<UserAccountResult> queryResult;
        try {
            queryResult = databaseServiceJDBC.executeQuery(query);
        } catch (PostgresExecutionException e) {
            throw GraphQLRequestException.createInternal(e);
        }
        ImmutableList<UserAccountResult> fetchedUsers = queryResult.deserialize();
        if(fetchedUsers.isEmpty()){
            return loginFailed(userInfo.remoteAddress(), captchaToken, loginMutationField);
        }
        UserAccountResult user = fetchedUsers.iterator().next();
        String storedPassword = ((io.loppi.includablevalues.StringValue) user.getHashedPassword()).getValue();
        String passwordBcrypt = loginInput.passwordBcrypt();
        byte[] digest = sha256Digest.digest(passwordBcrypt.getBytes(StandardCharsets.UTF_8));
        String password_bcrypt_sha256_base64 = Base64.getEncoder().encodeToString(digest);
        boolean matches = storedPassword.equals(password_bcrypt_sha256_base64);
        if(!matches){
            return loginFailed(userInfo.remoteAddress(), captchaToken, loginMutationField);
        }
        UUID userId = ((UUIDValue) user.getUserAccountId()).getValue();
        UUID sessionId = UUID.randomUUID();
        LoginSessionToInsert userSessionToInsert = new LoginSessionToInsert(new RetailDemoIdReferenceUUID(userId));
        userSessionToInsert.setLoginSessionIdIncludableOf(sessionId);
        GraphQLExecutableMutationField mutationField = customerGraphQLService.createExecutableCustomMutationField(
                        List.of(userSessionToInsert), new LoginResult(false, true), loginMutationField);
        CookieToAdd loginSessionCookie = new CookieToAdd(Application.SESSION_TOKEN_COOKIE_NAME, sessionId.toString());
        Metrics.incrementLoginSuccess();
        return new MutationFieldAndCookies(mutationField, List.of(loginSessionCookie));
    }

    private MutationFieldAndCookies loginFailed(
            InetAddress remoteAddress, String captchaToken, LoginMutationField loginMutationField) {
        Metrics.incrementLoginError();
        rateLimiter.consumeLoginFailedGraphQL(remoteAddress, captchaToken);
        LoginResult failedLoginResult = new LoginResult(false, false);
        GraphQLExecutableMutationField executableMutationField =
                customerGraphQLService.createExecutableCustomMutationField(List.of(), failedLoginResult, loginMutationField);
        return new MutationFieldAndCookies(executableMutationField, List.of());
    }

    public static GraphQLTypeConfiguration<LoginSessionResult> createStaffApiTypeConfiguration(LoginSessionMeta loginSessionMeta) {
        Set<Attribute<LoginSessionResult, ?>> outputAttributes = Set.of(loginSessionMeta.createdAt, loginSessionMeta.userAccount,
                loginSessionMeta.isSignedOut, loginSessionMeta.lastUpdatedAt);
        GraphQLDatabaseQueryConfiguration<LoginSessionResult> queryConfiguration = new GraphQLDatabaseQueryConfiguration<>(outputAttributes)
                .setIncludePluralRootField(true)
                .setIncludeOrderBy(Set.of(loginSessionMeta.createdAt, loginSessionMeta.lastUpdatedAt))
                .setIncludeWhere(Set.of(loginSessionMeta.isSignedOut, loginSessionMeta.createdAt,
                        loginSessionMeta.lastUpdatedAt, loginSessionMeta.userAccountId), false)
                .setPluralFieldDescription("Returns all login sessions from all users.");
        return new GraphQLTypeConfiguration<>(loginSessionMeta.getEntityMetadata()).setIncludeQuery(queryConfiguration);
    }
}