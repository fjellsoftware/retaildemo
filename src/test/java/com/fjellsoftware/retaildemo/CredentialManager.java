/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fjellsoftware.javafunctionalutils.opt.Opt;
import com.fjellsoftware.bcryptclientsalt.BCrypt;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class CredentialManager {

    private UUID captchaCache;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final String hCaptchaDevTokenResponse = "10000000-aaaa-bbbb-cccc-000000000001";
    private final URI customerURI;
    private final URI staffURI;
    private final HttpClient httpClient;

    public CredentialManager(){
        this.customerURI = CustomerApiTests.applicationURI;
        this.staffURI = StaffApiTests.applicationURI;
        this.httpClient = HttpClient.newHttpClient();
    }

    public static String createHCaptchaCookieValue(UUID token){
        return TestDependencies.hCaptchaCookieName + "=" + token.toString();
    }

    public static Cookie createSessionCookieFromCredentials(UUID sessionToken){
        return new Cookie(TestDependencies.sessionTokenCookieName, sessionToken.toString());
    }

    public UUID verifyCaptchaOrGetCache(){
        if(captchaCache != null){
            return captchaCache;
        }
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("token", hCaptchaDevTokenResponse);
            String graphQLPostBody = GraphQLUtils.createGraphQLPostBody(CustomerApiTests.fullDocumentStringByOperationName, "captcha", variables);
            HttpRequest httpRequest = HttpRequest.newBuilder(customerURI)
                    .POST(HttpRequest.BodyPublishers.ofString(graphQLPostBody))
                    .headers(TestDependencies.baseHeaders)
                    .build();
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String body = httpResponse.body();
            Map graphQLResponse = objectMapper.readValue(body, Map.class);
            Map data = (Map) graphQLResponse.get("data");
            Map captcha = (Map) data.get("verifyCaptcha");
            String tokenStringResult = (String) captcha.get("token");
            UUID.fromString(tokenStringResult);
            HttpHeaders headers = httpResponse.headers();
            Map<String, List<String>> headersMap = headers.map();
            List<String> setCookieHeaders = headersMap.get("set-cookie");
            assert setCookieHeaders != null && setCookieHeaders.size() == 1;
            String setCookieHeaderValue = setCookieHeaders.iterator().next();
            assert setCookieHeaderValue.startsWith(TestDependencies.hCaptchaCookieName);
            int uuidValueStartIndex = TestDependencies.hCaptchaCookieName.length() + 1;
            String token = setCookieHeaderValue.substring(uuidValueStartIndex, uuidValueStartIndex + UUID.randomUUID().toString().length());
            UUID validToken = UUID.fromString(token);
            captchaCache = validToken;
            return validToken;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public HttpResponse<String> loginWithSalt(
            String username, String passwordPlaintext, String hCaptchaCookieRequestHeaderValue, String salt) throws Exception{
        String hashedPassword = BCrypt.hashpw(passwordPlaintext, salt);
        Map<String,Object> loginVariables = new HashMap<>();
        loginVariables.put("username", username);
        loginVariables.put("passwordBcrypt", hashedPassword);
        String loginOperationBody = GraphQLUtils.createGraphQLPostBody(
                CustomerApiTests.fullDocumentStringByOperationName, "login", loginVariables);
        HttpRequest loginHttpRequest = HttpRequest.newBuilder(customerURI)
                .POST(HttpRequest.BodyPublishers.ofString(loginOperationBody))
                .headers(TestDependencies.baseHeaders)
                .header("Cookie", hCaptchaCookieRequestHeaderValue)
                .build();
        return HttpClient.newHttpClient().send(loginHttpRequest, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> login(String username, String passwordPlaintext,
                                      String hCaptchaCookieRequestHeaderValue)
            throws Exception {
        String salt = executeGetSalt(username);
        return loginWithSalt(username, passwordPlaintext, hCaptchaCookieRequestHeaderValue, salt);
    }

    public String executeGetSalt(String username) throws Exception {
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        String graphQLPostBody = GraphQLUtils.createGraphQLPostBody(
                CustomerApiTests.fullDocumentStringByOperationName, "getSalt", variables);
        HttpRequest httpRequest = HttpRequest.newBuilder(customerURI)
                .POST(HttpRequest.BodyPublishers.ofString(graphQLPostBody))
                .headers(TestDependencies.baseHeaders)
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        Map map = objectMapper.readValue(httpResponse.body(), Map.class);
        Map data = (Map) map.get("data");
        Map getBcryptSalt = (Map) data.get("getBcryptSalt");
        String salt = (String) getBcryptSalt.get("salt");
        return salt;
    }

    public TestCredentials signUpCustomerAndCheckLogin(UUID hCaptchaToken){
        try {
            Map<String, Object> variables = new HashMap<>();
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(hCaptchaToken.toString().getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getEncoder().encodeToString(hash).substring(0, 10);
            String username = "john.doe_" + encoded;
            String passwordPlaintext = UUID.randomUUID().toString();
            String salt = executeGetSalt(username);
            String hashedPassword = BCrypt.hashpw(passwordPlaintext, salt);
            variables.put("username", username);
            variables.put("passwordBcrypt", hashedPassword);

            String graphQLPostBody = GraphQLUtils.createGraphQLPostBody(CustomerApiTests.fullDocumentStringByOperationName, "signUp", variables);
            HttpRequest httpRequest = HttpRequest.newBuilder(customerURI)
                    .POST(HttpRequest.BodyPublishers.ofString(graphQLPostBody))
                    .headers(TestDependencies.baseHeaders)
                    .header("Cookie", createHCaptchaCookieValue(hCaptchaToken))
                    .build();
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> responseMap = objectMapper.readValue(httpResponse.body(), Map.class);
            assert responseMap.size() == 1;
            Map.Entry<String, Object> entry = responseMap.entrySet().iterator().next();
            assert entry.getKey().equals("data");
            Map<String, Object> dataValue = (Map<String, Object>) entry.getValue();
            assert dataValue.size() == 1;
            Map.Entry<String, Object> innerEntry = dataValue.entrySet().iterator().next();
            assert innerEntry.getKey().equals("signUp");
            Map value = (Map) innerEntry.getValue();
            Set<Map.Entry> entrySet = value.entrySet();
            assert entrySet.size() == 1;
            Map.Entry next = entrySet.iterator().next();
            assert next.getKey().equals("succeeded");
            assert ((Boolean) next.getValue()).equals(true);
            Map<String, List<String>> headersMap = httpResponse.headers().map();
            List<String> setCookieHeaders = headersMap.get("set-cookie");
            assert setCookieHeaders != null && setCookieHeaders.size() == 1;
            String setCookieHeaderValue = setCookieHeaders.iterator().next();
            assert setCookieHeaderValue.startsWith(TestDependencies.sessionTokenCookieName);
            int uuidValueStartIndex = TestDependencies.sessionTokenCookieName.length() + 1;
            String token = setCookieHeaderValue.substring(uuidValueStartIndex, uuidValueStartIndex + UUID.randomUUID().toString().length());
            UUID validToken = UUID.fromString(token);

            String currentUserResponseBody = getCurrentUser(Opt.of(validToken));
            Map<String,Object> currentUserResponseMap = objectMapper.readValue(currentUserResponseBody, Map.class);
            assert currentUserResponseMap.size() == 1;
            Map<String, Object> currentUserData = (Map<String, Object>) currentUserResponseMap.get("data");
            List<Map<String, Object>> tryCurrentUser = (List<Map<String, Object>>) currentUserData.get("tryCurrentUser");
            assert tryCurrentUser.size() == 1;
            Map<String, Object> user = tryCurrentUser.iterator().next();
            UUID userAccountId = UUID.fromString((String) user.get("userAccountId"));

            return new TestCredentials(username, passwordPlaintext, validToken, userAccountId);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public String getCurrentUser(Opt<UUID> sessionTokenOpt){
        return TestUtils.executeGraphQLRequestWithHeadersGetResultBody(CustomerApiTests.applicationURI,
                CustomerApiTests.fullDocumentStringByOperationName.get("currentUser").getOrThrow(), null, sessionTokenOpt);
    }


    public HttpResponse<String> tryUpgradeToStaff(UUID sessionToken) {
        return executeCustomerAPIRequestWithSessionCookie(sessionToken, "upgradeToStaff");
    }

    public HttpResponse<String> tryDowngradeToCustomer(UUID sessionToken) {
        return executeCustomerAPIRequestWithSessionCookie(sessionToken, "downgradeToCustomer");
    }

    private HttpResponse<String> executeCustomerAPIRequestWithSessionCookie(UUID sessionToken, String operationName){
        try {
            Cookie cookie = createSessionCookieFromCredentials(sessionToken);
            String graphQLPostBody = GraphQLUtils.createGraphQLPostBody(CustomerApiTests.fullDocumentStringByOperationName, operationName, null);
            HttpRequest httpRequest = HttpRequest.newBuilder(customerURI)
                    .POST(HttpRequest.BodyPublishers.ofString(graphQLPostBody))
                    .headers(TestDependencies.baseHeaders)
                    .header(cookie.headerName(), cookie.headerValue())
                    .build();
            return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    public HttpResponse<String> tryGetAllLoginSessions(UUID sessionToken){
        try {
            Cookie cookie = createSessionCookieFromCredentials(sessionToken);
            String graphQLPostBody = GraphQLUtils.createGraphQLPostBody(StaffApiTests.fullDocumentStringByOperationName, "loginSessions", null);
            HttpRequest httpRequest = HttpRequest.newBuilder(staffURI)
                    .POST(HttpRequest.BodyPublishers.ofString(graphQLPostBody))
                    .headers(TestDependencies.baseHeaders)
                    .header(cookie.headerName(), cookie.headerValue())
                    .build();
            return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        }catch (Exception e){
            throw new RuntimeException();
        }
    }
}
