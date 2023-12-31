/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.domain;

import com.fjellsoftware.retaildemo.authorizationcommon.HCaptchaVerifier;
import com.fjellsoftware.retaildemo.autogenerated.graphql.customer.*;
import com.fjellsoftware.retaildemo.autogenerated.orm.*;
import io.loppi.graphql.schema.*;
import io.loppi.orm.*;
import io.loppi.graphql.*;
import io.loppi.orm.metamodel.Attribute;
import io.loppi.orm.query.DatabaseQuery;
import com.fjellsoftware.retaildemo.Application;
import com.fjellsoftware.retaildemo.authorizationcommon.cookie.CookieToAdd;
import com.fjellsoftware.javafunctionalutils.ImmutableList;
import com.fjellsoftware.retaildemo.CoreDependencies;
import com.fjellsoftware.retaildemo.authorizationcommon.HCaptchaCache;
import com.fjellsoftware.retaildemo.authorizationcommon.MutationFieldAndCookies;
import com.fjellsoftware.retaildemo.authorizationcommon.userinfo.AuthenticatedUser;
import com.fjellsoftware.retaildemo.authorizationcommon.userinfo.CaptchaUser;
import com.fjellsoftware.retaildemo.authorizationcommon.userinfo.UserInfo;
import com.fjellsoftware.bcryptclientsalt.BCryptClientSalt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UserDomain {

    private final LoppiServiceRetailDemo loppiService;
    private final UserAccountMeta userAccountMeta;
    private final MessageDigest sha256Digest;
    private final HCaptchaCache hCaptchaCache;
    private final CustomerGraphQLService customerGraphQLService;
    private final String hostName;

    public UserDomain(LoppiServiceRetailDemo loppiService, UserAccountMeta userAccountMeta,
                      HCaptchaCache hCaptchaCache, MessageDigest sha256Digest,
                      CustomerGraphQLService customerGraphQLService, String hostName) {
        this.loppiService = loppiService;
        this.userAccountMeta = userAccountMeta;
        this.hCaptchaCache = hCaptchaCache;
        this.sha256Digest = sha256Digest;
        this.customerGraphQLService = customerGraphQLService;
        this.hostName = hostName;
    }

    private static final String apiBaseName = "User";
    private static final String apiPluralName = "Users";
    public static GraphQLTypeConfiguration<UserAccountResult> createCustomerApiTypeConfiguration(UserAccountMeta userAccountMeta){
        Set<Attribute<UserAccountResult, ?>> outputAttributes = Set.of(userAccountMeta.userAccountId, userAccountMeta.name,
                userAccountMeta.username, userAccountMeta.createdAt, userAccountMeta.role);
        GraphQLDatabaseQueryConfiguration<UserAccountResult> queryConfiguration =
                new GraphQLDatabaseQueryConfiguration<>(outputAttributes)
                        .setIncludePluralRootField(true)
                        .setPluralFieldNameOverride("tryCurrentUser")
                        .setPluralFieldDescription("If logged in, this returns a list with one user which is the " +
                                "current user, otherwise this returns an empty list.");
        return new GraphQLTypeConfiguration<>(userAccountMeta.getEntityMetadata())
                .setIncludeQuery(queryConfiguration)
                .addFieldNameAttributeOverride(userAccountMeta.hashedPassword, "password")
                .setTypeName(apiBaseName, apiPluralName);
    }

    public static GraphQLFieldDefinition createUpdatePasswordCustomMutationDefinition(){
        GraphQLArgumentDefinition previousPasswordArgument = GraphQLArgumentDefinition.builder()
                .setName("previousPassword").setNotNull(true)
                .setType(GraphQLScalarTypeCategory.String).build();
        GraphQLArgumentDefinition updatedPasswordArgument = GraphQLArgumentDefinition.builder()
                .setName("updatedPassword").setNotNull(true)
                .setType(GraphQLScalarTypeCategory.String).build();

        return GraphQLFieldDefinition.builder().setName("updatePassword")
                .setDescription("Currently not supported.")
                .setNotNull(true).setType(IgnoredResult.getGraphQLObjectType())
                .setArguments(List.of(previousPasswordArgument, updatedPasswordArgument)).build();
    }

    public static GraphQLFieldDefinition createSignUpCustomMutationDefiniton(){
        GraphQLArgumentDefinition nameArgument = GraphQLArgumentDefinition.builder().setName("name").setNotNull(true)
                .setType(GraphQLScalarTypeCategory.String).build();
        GraphQLArgumentDefinition usernameArgument = GraphQLArgumentDefinition.builder().setName("username").setNotNull(true)
                .setType(GraphQLScalarTypeCategory.String).build();
        GraphQLArgumentDefinition passwordBcryptArgument = GraphQLArgumentDefinition.builder().setName("passwordBcrypt").setNotNull(true)
                .setType(GraphQLScalarTypeCategory.String).build();

        GraphQLFieldDefinition successfulField = GraphQLFieldDefinition.builder().setName("succeeded")
                .setType(GraphQLScalarTypeCategory.Boolean).setNotNull(true).build();
        GraphQLFieldDefinition usernameTakenField = GraphQLFieldDefinition.builder().setName("usernameTaken")
                .setType(GraphQLScalarTypeCategory.Boolean).setNotNull(true).build();
        GraphQLObjectType signUpResultType = GraphQLObjectType.builder().setName("signUpResult")
                .addField(successfulField)
                .addField(usernameTakenField)
                .build();
        return GraphQLFieldDefinition.builder().setName("signUp")
                .setDescription(String.format("Signs up for the service. Requires solved captcha, with token sent via cookie \"%s\". " +
                        "Also requires password to be hashed using bcrypt and the salt from the \"%s\" field.",
                                HCaptchaVerifier.H_CAPTCHA_TOKEN_COOKIE_NAME, CoreDependencies.GET_BCRYPT_SALT_FIELD_NAME))
                .setNotNull(true)
                .setType(signUpResultType)
                .setArguments(List.of(nameArgument, usernameArgument, passwordBcryptArgument)).build();
    }

    public GraphQLExecutableQueryField applyCustomerAccessControlForUserQuery(
            TryCurrentUserQueryField tryCurrentUserQueryField, UserInfo userInfo) {
        UUID accountId = UUID.randomUUID();
        if (userInfo instanceof AuthenticatedUser authenticatedUser) {
            accountId = authenticatedUser.userId();
        }
        GraphQLDatabaseQueryFieldSimpleArguments<UserAccountResult> userQuery = tryCurrentUserQueryField.queryField();
        return GraphQLDatabaseQuerySimpleArgumentBuilder.from(userQuery)
                .where().eq(userAccountMeta.userAccountId, accountId).end()
                .build();
    }

    public MutationFieldAndCookies handleSignUp(
            UserInfo userInfo, SignUpMutationField mutationField) throws GraphQLRequestException {
        if(userInfo instanceof AuthenticatedUser){
            throw new GraphQLRequestException("Not allowed to sign up while signed in.", GraphQLErrorTypeCategory.OTHER);
        }
        if(!(userInfo instanceof CaptchaUser captchaUser)) {
            throw new GraphQLRequestException("Invalid captcha token. " +
                    "Please go back and solve the captcha.", GraphQLErrorTypeCategory.OTHER);
        }
        boolean isValid = hCaptchaCache.checkTokenValidForSignUp(captchaUser.captchaToken());
        if(!isValid){
            throw new GraphQLRequestException("Captcha token already used for sign-up. Solve captcha again.",
                    GraphQLErrorTypeCategory.VALIDATION);
        }
        SignUpMutationInput input = mutationField.input();
        String name = input.name();
        if (!name.equals("John Doe")) {
            throw new GraphQLRequestException("User must be called John Doe in this demo.", GraphQLErrorTypeCategory.OTHER);
        }

        // In this demo the username should be for example: john.doe_YmFzZTY0IG,
        // where the gibberish string is the 10 first characters in base64(hashSHA256(captchaToken)).
        String username = input.username();
        int numberOfHashedTokenChars = 10;
        String captchaToken = captchaUser.captchaToken();
        byte[] hash = sha256Digest.digest(captchaToken.getBytes(StandardCharsets.UTF_8));
        String encoded = Base64.getEncoder().encodeToString(hash).substring(0,numberOfHashedTokenChars);
        String usernameStart = "john.doe_";
        boolean usernameIsCorrect = username.length() == usernameStart.length() + numberOfHashedTokenChars
                && username.startsWith(usernameStart) && username.contains(encoded);
        if (!usernameIsCorrect) {
            throw new GraphQLRequestException(String.format("Username must be [%s] in this demo.",
                    usernameStart + encoded), GraphQLErrorTypeCategory.OTHER);
        }
        String passwordBcrypt = input.passwordBcrypt();
        int maximumPasswordLength = 100;
        if(passwordBcrypt.length() > maximumPasswordLength){
            throw new GraphQLRequestException(String.format("Password too long. Maximum length is [%s]",
                    maximumPasswordLength), GraphQLErrorTypeCategory.OTHER);
        }
        String expectedSalt = BCryptClientSalt.fromServiceIdentifierAndUsername(hostName, username);
        if(!passwordBcrypt.startsWith(expectedSalt)){
            throw new GraphQLRequestException("Expected the password to be hashed using BCrypt, and the " +
                    "salt to be from the graphql getBcryptSalt mutation field, but the salt found was incorrect.",
                    GraphQLErrorTypeCategory.VALIDATION);
        }
        DatabaseServiceJDBCRetailDemo databaseServiceJDBC = loppiService.getDatabaseServiceJDBC();
        DatabaseQuery<UserAccountResult> query = databaseServiceJDBC.createQueryBuilder(UserAccountResult.class, 1)
                .includeScalar(userAccountMeta.userAccountId)
                .where().eq(userAccountMeta.username, username).end()
                .build();
        QueryExecutionResult<UserAccountResult> queryResult;
        try {
            queryResult = databaseServiceJDBC.executeQuery(query);
        } catch (PostgresExecutionException e) {
            throw GraphQLRequestException.createInternal(e);
        }
        ImmutableList<UserAccountResult> fetchedUsers = queryResult.deserialize();
        if(!fetchedUsers.isEmpty()){
            GraphQLExecutableMutationField executableMutationField = customerGraphQLService
                    .createExecutableCustomMutationField(List.of(), new SignUpResult(true, false), mutationField);
            return new MutationFieldAndCookies(executableMutationField, List.of());
        }
        byte[] digest = sha256Digest.digest(passwordBcrypt.getBytes(StandardCharsets.UTF_8));
        String password_bcrypt_sha256_base64 = Base64.getEncoder().encodeToString(digest);
        UserAccountToInsert userAccountToInsert = new UserAccountToInsert(name, username, password_bcrypt_sha256_base64);

        UUID sessionId = UUID.randomUUID();
        LoginSessionToInsert userSessionToInsert = new LoginSessionToInsert(userAccountToInsert);
        userSessionToInsert.setLoginSessionIdIncludableOf(sessionId);
        CookieToAdd loginSessionCookie = new CookieToAdd(Application.SESSION_TOKEN_COOKIE_NAME, sessionId.toString());
        GraphQLExecutableMutationField executableMutationField =
                customerGraphQLService.createExecutableCustomMutationField(
                List.of(userAccountToInsert, userSessionToInsert), new SignUpResult(false, true), mutationField);
        hCaptchaCache.consumeTokenForSignUp(captchaUser.captchaToken());
        return new MutationFieldAndCookies(executableMutationField, List.of(loginSessionCookie));
    }

    public MutationFieldAndCookies handleUpdatePassword(
            UserInfo userInfo, UpdatePasswordMutationField updatePassword)
            throws GraphQLRequestException {
        throw new GraphQLRequestException("Password reset is not supported in this demo.", GraphQLErrorTypeCategory.OTHER);
    }

    public static GraphQLTypeConfiguration<UserAccountResult> createStaffApiTypeConfiguration(UserAccountMeta userAccountMeta) {
        Set<Attribute<UserAccountResult, ?>> outputAttributes = Set.of(userAccountMeta.userAccountId,
                userAccountMeta.name, userAccountMeta.username,
                userAccountMeta.createdAt, userAccountMeta.lastUpdatedAt, userAccountMeta.role, userAccountMeta.loginSessions);
        GraphQLDatabaseQueryConfiguration<UserAccountResult> queryConfiguration = new GraphQLDatabaseQueryConfiguration<>(outputAttributes)
                .setIncludePluralRootField(true)
                .setIncludeWhere(Set.of(userAccountMeta.username, userAccountMeta.userAccountId, userAccountMeta.name,
                        userAccountMeta.role), false)
                .setIncludeOrderBy(Set.of(userAccountMeta.userAccountId, userAccountMeta.createdAt, userAccountMeta.lastUpdatedAt))
                .setPluralFieldDescription("Returns all registered users.");
        return new GraphQLTypeConfiguration<>(userAccountMeta.getEntityMetadata())
                .setIncludeQuery(queryConfiguration)
                .setTypeName(apiBaseName, apiPluralName);
    }
}
