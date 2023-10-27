/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.loppi.orm.PostgresExecutionException;
import com.fjellsoftware.javafunctionalutils.ImmutableMap;
import com.fjellsoftware.javafunctionalutils.opt.Opt;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.*;

import static com.fjellsoftware.retaildemo.TestUtils.createTestURI;
import static com.fjellsoftware.retaildemo.TestUtils.getMethodName;

@TestMethodOrder(MethodOrderer.MethodName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CustomerApiTests {

    public static final String resourceFilesFolderName = "customerApi";
    public static final ImmutableMap<String, String> fullDocumentStringByOperationName =
            TestUtils.initializeOperationsByName(resourceFilesFolderName);
    public static final ImmutableMap<String, Map<String,Object>> resultsMap =
            TestUtils.initializeResults(resourceFilesFolderName);
    public static final URI applicationURI = createTestURI("/api/v1/graphql");

    private final TestDependencies testDependencies;
    private final CredentialManager credentialManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String hCaptchaCookieRequestHeaderValue;
    private UUID hCaptchaToken;
    private TestCredentials mainCustomerTestCredentials;

    public CustomerApiTests() {
        this.testDependencies = new TestDependencies(false);
        this.credentialManager = new CredentialManager();
    }

    @AfterAll
    public void cleanup() throws PostgresExecutionException {
        TestUtils.cleanup(testDependencies);
    }

    static final String getLatestProductsOperationName = "getProducts";
    @Test
    public void getProducts() {
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get(getLatestProductsOperationName).getOrThrow(), null, Opt.empty(),
                resultsMap.get(getLatestProductsOperationName).getOrThrow());
    }

    @Test
    public void _1_solveCaptcha() {
        hCaptchaToken = credentialManager.verifyCaptchaOrGetCache();
        hCaptchaCookieRequestHeaderValue = CredentialManager.createHCaptchaCookieValue(hCaptchaToken);
    }

    @Test
    public void solveCaptchaFailure(TestInfo testInfo) throws IOException{
        Map<String, Object> variables = new HashMap<>();
        variables.put("token", "10000000-aaaa-bbbb-cccc-000000000123");
        String body = TestUtils.executeGraphQLRequestWithHeadersGetResultBody(applicationURI,
                fullDocumentStringByOperationName.get("captcha").getOrThrow(), variables, Opt.empty());
        System.out.println(body);
        Map graphQLResponse = objectMapper.readValue(body, Map.class);
        Map solveCaptchaFailureMap = resultsMap.get(getMethodName(testInfo)).getOrThrow();
        assert solveCaptchaFailureMap != null;
        assert graphQLResponse.equals(solveCaptchaFailureMap);
    }

    @Test
    public void _2_signUpAndCheckCurrentUser() throws Exception {
        String unauthenticatedCurrentUser = credentialManager.getCurrentUser(Opt.empty());
        System.out.println(unauthenticatedCurrentUser);
        Map unauthenticatedCurrentUserResponseMap = objectMapper.readValue(unauthenticatedCurrentUser, Map.class);
        assert unauthenticatedCurrentUserResponseMap.equals(resultsMap.get("zeroUsers").getOrThrow());
        if(hCaptchaToken == null){
            _1_solveCaptcha();
        }
        mainCustomerTestCredentials = credentialManager.signUpCustomerAndCheckLogin(hCaptchaToken);
        String authenticatedCurrentUserResponse = credentialManager.getCurrentUser(Opt.of(mainCustomerTestCredentials.sessionToken()));
        System.out.println(authenticatedCurrentUserResponse);
        checkCurrentUserResponseIsUserByCredentials(authenticatedCurrentUserResponse, mainCustomerTestCredentials);
    }


    private void checkCurrentUserResponseIsUserByCredentials(String httpResponseBody, TestCredentials testCredentials) throws JsonProcessingException {
        Map<String,Object> authenticatedCurrentUserResponseMap = objectMapper.readValue(httpResponseBody, Map.class);
        assert authenticatedCurrentUserResponseMap.size() == 1;
        Map<String, Object> data = (Map<String, Object>) authenticatedCurrentUserResponseMap.get("data");
        assert data.size() == 1;
        List<Map<String, Object>> currentUserList = (List<Map<String, Object>>) data.get("tryCurrentUser");
        assert currentUserList.size() == 1;
        Map<String, Object> currentUserObject = currentUserList.iterator().next();
        assert currentUserObject.size() == 3;
        assert currentUserObject.get("userAccountId").equals(testCredentials.userAccountId().toString());
        assert currentUserObject.get("username").equals(testCredentials.username());
    }

    @Test
    public void loginInvalidUsername() throws Exception {
        HttpResponse<String> httpResponse = loginInternal("asdfijdfij@adsfgrm.com", "password");
        Map responseMap = objectMapper.readValue(httpResponse.body(), Map.class);
        assert responseMap.equals(resultsMap.get("loginFailed").getOrThrow());
    }

    @Test
    public void _3_loginWrongPassword() throws Exception {
        if (mainCustomerTestCredentials == null) {
            _2_signUpAndCheckCurrentUser();
        }
        HttpResponse<String> httpResponse = loginInternal(mainCustomerTestCredentials.username(), "password");
        Map responseMap = objectMapper.readValue(httpResponse.body(), Map.class);
        assert responseMap.equals(resultsMap.get("loginFailed").getOrThrow());
    }

    @Test
    public void _3_loginValidCredentials() throws Exception {
        if (mainCustomerTestCredentials == null) {
            _2_signUpAndCheckCurrentUser();
        }
        HttpResponse<String> httpResponse = loginInternal(mainCustomerTestCredentials.username(),
                mainCustomerTestCredentials.password());
        Map responseMap = objectMapper.readValue(httpResponse.body(), Map.class);
        assert responseMap.size() == 1;
        assert responseMap.get("data") instanceof Map;
        Map<String, List<String>> httpHeaders = httpResponse.headers().map();
        List<String> setCookieHeaders = httpHeaders.get("set-cookie");
        assert setCookieHeaders != null && setCookieHeaders.size() == 1;
        assert setCookieHeaders.iterator().next().startsWith(TestDependencies.sessionTokenCookieName);
    }

    private HttpResponse<String> loginInternal(String username, String password) throws Exception {
        return credentialManager.login(username, password, hCaptchaCookieRequestHeaderValue);
    }

    @Test
    public void zz3_signOut() throws Exception {
        if(mainCustomerTestCredentials == null) {
            _2_signUpAndCheckCurrentUser();
        }
        Opt<UUID> sessionTokenOpt = Opt.of(mainCustomerTestCredentials.sessionToken());
        String currentUserResponseBody = credentialManager.getCurrentUser(sessionTokenOpt);
        checkCurrentUserResponseIsUserByCredentials(currentUserResponseBody, mainCustomerTestCredentials);
        HttpResponse<String> signOutHttpResponse = TestUtils.executeGraphQLRequestWithHeadersGetResponse(applicationURI,
                fullDocumentStringByOperationName.get("signOut").getOrThrow(), null, sessionTokenOpt);
        String signOutBody = signOutHttpResponse.body();
        System.out.println(signOutBody);
        Map<String,Object> signOutResponse = objectMapper.readValue(signOutBody, Map.class);
        assert signOutResponse.size() == 1;
        Map<String, Object> data = (Map<String, Object>) signOutResponse.get("data");
        assert data.size() == 1;
        Map<String, Object> signOut = (Map<String, Object>) data.get("signOut");
        assert signOut.size() == 1;

        HttpHeaders headers = signOutHttpResponse.headers();
        Map<String, List<String>> headersMap = headers.map();
        List<String> setCookieHeaders = headersMap.get("set-cookie");
        assert setCookieHeaders != null;
        assert setCookieHeaders.size() == 1;
        String setCookieHeaderValue = setCookieHeaders.iterator().next();
        System.out.println(setCookieHeaderValue);
        assert setCookieHeaderValue.startsWith(TestDependencies.sessionTokenCookieName+"=; ");
        String invalidTokenResponseBody = credentialManager.getCurrentUser(sessionTokenOpt);
        System.out.println(invalidTokenResponseBody);
        Map<String,Object> invalidTokenResultMap = objectMapper.readValue(invalidTokenResponseBody, Map.class);
        assert invalidTokenResultMap.size() == 1;
        Map<String,Object> emptyUsersData = (Map<String,Object>) invalidTokenResultMap.get("data");
        assert emptyUsersData.size() == 1;
        List<Map<String, Object>> tryCurrentUser = (List<Map<String, Object>>) emptyUsersData.get("tryCurrentUser");
        assert tryCurrentUser.size() == 0;
    }

    @Test
    public void getOrdersUnauthenticatedFail() {
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get("getOrders").getOrThrow(), null, Opt.empty(),
                resultsMap.get(TestDependencies.notLoggedInKey).getOrThrow());
    }

    static final String defaultAddress = "123 Maple Street, Anytown";
    static final String defaultName = "John Doe";
    static final String defaultPhoneNumber = "+44 808 157 0192";

    public static final String placeOrderOperationName = "placeOrder";

    @Test
    public void _3_placeOrderWrongUnitprice() throws Exception {
        if(mainCustomerTestCredentials == null){
            _2_signUpAndCheckCurrentUser();
        }
        Map<String,Object> variables = new HashMap<>();
        variables.put("address", defaultAddress);
        variables.put("name", defaultName);
        variables.put("phoneNumber", defaultPhoneNumber);
        variables.put("countryId", 9);
        List<Map<String,Object>> orderLinesList = new ArrayList<>();
        orderLinesList.add(createOrderLineMap(38, 3, "0.12"));
        // Correct price is 0.12
        orderLinesList.add(createOrderLineMap(39, 1, "0.1"));
        orderLinesList.add(createOrderLineMap(40, 10, "0.19"));
        variables.put("orderLines", orderLinesList);

        String body = TestUtils.executeGraphQLRequestWithHeadersGetResultBody(applicationURI,
                fullDocumentStringByOperationName.get(placeOrderOperationName).getOrThrow(), variables,
                Opt.of(mainCustomerTestCredentials.sessionToken()));
        System.out.println(body);
        Map<String,Object> placeOrderResponseMap = objectMapper.readValue(body, Map.class);
        assert placeOrderResponseMap.size() == 1;
        List<Map<String, Object>> errors = (List) placeOrderResponseMap.get("errors");
        assert errors.size() == 1;
        Map<String, Object> error = errors.iterator().next();
        assert ((String) error.get("message")).contains("Invalid unit price for product id:");
        HttpResponse<String> getOrdersResponse = getOrdersWithCredentials(mainCustomerTestCredentials);
        Map getOrdersResponseMap = objectMapper.readValue(getOrdersResponse.body(), Map.class);
        System.out.println(getOrdersResponse.body());
        assert getOrdersResponseMap.equals(resultsMap.get("getOrdersNoOrders").getOrThrow());
    }

    @Test
    public void _4_placeOrderUnauthenticated() throws Exception {

        if(mainCustomerTestCredentials == null){
            _2_signUpAndCheckCurrentUser();
        }
        UUID mainSessionToken = mainCustomerTestCredentials.sessionToken();
        credentialManager.tryUpgradeToStaff(mainSessionToken);

        String bodyBeforeOrder = TestUtils.executeGraphQLRequestWithHeadersGetResultBody(StaffApiTests.applicationURI,
                fullDocumentStringByOperationName.get(StaffApiTests.getOrdersOperationName).getOrThrow(), null,
                Opt.of(mainSessionToken));
        Map<String,Object> getOrdersBeforePlaceMap = objectMapper.readValue(bodyBeforeOrder, Map.class);
        assert getOrdersBeforePlaceMap.size() == 1;
        assert getOrdersBeforePlaceMap.containsKey("data");

        Map<String, Object> placeOrderVariables = createPlaceOrderVariables(UUID.randomUUID(), false);
        String body = TestUtils.executeGraphQLRequestWithHeadersGetResultBody(applicationURI,
                fullDocumentStringByOperationName.get(placeOrderOperationName).getOrThrow(), placeOrderVariables,
                Opt.empty());
        Map<String,Object> placeOrderResponseMap = objectMapper.readValue(body, Map.class);
        assert placeOrderResponseMap.size() == 1;
        assert placeOrderResponseMap.containsKey("data");

        String bodyAfterOrder = TestUtils.executeGraphQLRequestWithHeadersGetResultBody(StaffApiTests.applicationURI,
                fullDocumentStringByOperationName.get(StaffApiTests.getOrdersOperationName).getOrThrow(), null,
                Opt.of(mainSessionToken));

        Map<String,Object> getOrdersAfterPlaceMap = objectMapper.readValue(bodyBeforeOrder, Map.class);
        assert getOrdersAfterPlaceMap.size() == 1;
        assert getOrdersAfterPlaceMap.containsKey("data");

        assert !bodyBeforeOrder.equals(bodyAfterOrder);
        credentialManager.tryDowngradeToCustomer(mainSessionToken);
    }

    @Test
    public void _4_placeOrderGetOrdersWithCredentials() throws Exception {
        if(mainCustomerTestCredentials == null){
            _2_signUpAndCheckCurrentUser();
        }
        HttpResponse<String> getOrdersResponse = getOrdersWithCredentials(mainCustomerTestCredentials);
        Map getOrdersResponseMap = objectMapper.readValue(getOrdersResponse.body(), Map.class);
        System.out.println(getOrdersResponse.body());
        assert getOrdersResponseMap.equals(resultsMap.get("getOrdersNoOrders").getOrThrow());
        Map<String, Object> variables = createPlaceOrderVariables(mainCustomerTestCredentials.userAccountId(), true);
        String body = TestUtils.executeGraphQLRequestWithHeadersGetResultBody(applicationURI,
                fullDocumentStringByOperationName.get(placeOrderOperationName).getOrThrow(), variables,
                Opt.of(mainCustomerTestCredentials.sessionToken()));
        Map<String,Object> placeOrderResponseMap = objectMapper.readValue(body, Map.class);
        assert placeOrderResponseMap.size() == 1;
        Map<String, Object> data = (Map<String, Object>) placeOrderResponseMap.get("data");
        assert data.size() == 1;
        Map<String, Object> placeOrder = (Map<String, Object>) data.get(placeOrderOperationName);
        assert placeOrder.size() == 1;
        String purchaseOrderIdKey = "purchaseOrderId";
        int purchaseOrderId = (int) placeOrder.get(purchaseOrderIdKey);
        HttpResponse<String> getOrdersWithOrdersResponse = getOrdersWithCredentials(mainCustomerTestCredentials);
        String getOrdersWithOrdersResponseBody = getOrdersWithOrdersResponse.body();
        System.out.println(getOrdersWithOrdersResponseBody);
        Map<String,Object> getOrdersWithOrdersResponseMap = objectMapper.readValue(getOrdersWithOrdersResponseBody, Map.class);
        Map order = (Map) ((List) ((Map) getOrdersWithOrdersResponseMap.get("data")).get("orders")).iterator().next();
        assert order.get(purchaseOrderIdKey).equals(purchaseOrderId);
        order.remove(purchaseOrderIdKey);

        Map<String, Object> expectedPlaceOrderWithoutOrderId = resultsMap.get("placeOrderWithoutOrderId").getOrThrow();
        Map<String, Object> expectedData = (Map<String, Object>) expectedPlaceOrderWithoutOrderId.get("data");
        Map<String, Object> actualData = (Map<String, Object>) getOrdersWithOrdersResponseMap.get("data");
        List<Map<String,Object>> expectedOrders = (List) expectedData.get("orders");
        List<Map<String,Object>> actualOrders = (List) actualData.get("orders");
        assert expectedOrders.size() == actualOrders.size();
        Iterator<Map<String, Object>> actualOrdersIterator = actualOrders.iterator();
        for (Map<String, Object> expectedOrder : expectedOrders) {
            List expectedOrderLines = (List) expectedOrder.get("orderLines");
            Map<String, Object> actualOrder = actualOrdersIterator.next();
            List actualOrderLines = (List) actualOrder.get("orderLines");
            assert expectedOrderLines.size() == actualOrderLines.size();
            for (int i = 0; i < expectedOrderLines.size(); i++) {
                Map expectedOrderLine = (Map) expectedOrderLines.get(i);
                assert actualOrderLines.contains(expectedOrderLine);
            }
        }
        HttpResponse<String> getOrdersWithOrderResponse = getOrdersWithCredentials(mainCustomerTestCredentials);
        String getOrdersWithOrderResponseBody = getOrdersWithOrderResponse.body();
        Map getOrdersWithOrderResponseMap = objectMapper.readValue(getOrdersWithOrderResponseBody, Map.class);
        System.out.println(getOrdersWithOrderResponseBody);
        List orders = (List) ((Map) getOrdersWithOrderResponseMap.get("data")).get("orders");
        assert orders.size() == 1;
    }

    public static Map<String,Object> createPlaceOrderVariables(UUID userAccountId, boolean includeUserAccount){
        Map<String,Object> variables = new HashMap<>();
        variables.put("countryId", 9);
        variables.put("address", defaultAddress);
        variables.put("name", defaultName);
        variables.put("phoneNumber", defaultPhoneNumber);
        List<Map<String,Object>> orderLinesList = new ArrayList<>();
        orderLinesList.add(createOrderLineMap(38, 3, "0.12"));
        orderLinesList.add(createOrderLineMap(39, 1, "0.12"));
        orderLinesList.add(createOrderLineMap(40, 10, "0.19"));
        variables.put("orderLines", orderLinesList);
        return variables;
    }

    public static Map<String,Object> createOrderLineMap(int productId, int quantity, String unitprice){
        Map<String,Object> orderLine = new HashMap<>();
        orderLine.put("productId", productId);
        orderLine.put("quantity", quantity);
        orderLine.put("unitPrice", unitprice);
        return orderLine;
    }

    private HttpResponse<String> getOrdersWithCredentials(TestCredentials credentials){
        return TestUtils.executeGraphQLRequestWithHeadersGetResponse(applicationURI,
                fullDocumentStringByOperationName.get("getOrders").getOrThrow(), null, Opt.of(credentials.sessionToken()));
    }

    // Demo specific tests

    // zz to make sure it runs among the last tests.
    // We don't want to upgrade customer user to staff before most of the other tests are run.
    @Test
    public void zz1_upgradeToStaff() throws JsonProcessingException {
        // 1. try to get info about all sessions, and submit logged in customer session,
        //      and expect error since it should require staff session.
        // 2. upgrade current user to staff, and expect non-error result
        // 3. try to get info about all sessions again and expect success, with number of sessions greater than zero,
        //      because we know at least one session (the current one) is active.

        checkGetLoginSessionsFailure();

        HttpResponse<String> httpResponse = credentialManager.tryUpgradeToStaff(mainCustomerTestCredentials.sessionToken());
        String httpBody = httpResponse.body();
        Map<String, Object> upgradeToStaffExpected = resultsMap.get("upgradeToStaff").getOrThrow();
        Map actualResult = objectMapper.readValue(httpBody, Map.class);
        assert actualResult.equals(upgradeToStaffExpected);

        checkGetLoginSessionsSuccess();
    }

    @Test
    public void zz2_downgradeToCustomer() throws JsonProcessingException {
        checkGetLoginSessionsSuccess();

        HttpResponse<String> httpResponse = credentialManager.tryDowngradeToCustomer(mainCustomerTestCredentials.sessionToken());
        String httpBody = httpResponse.body();
        Map<String, Object> upgradeToStaffExpected = resultsMap.get("downgradeToCustomer").getOrThrow();
        Map actualResult = objectMapper.readValue(httpBody, Map.class);
        assert actualResult.equals(upgradeToStaffExpected);

        checkGetLoginSessionsFailure();
    }

    private void checkGetLoginSessionsSuccess(){
        try {
            HttpResponse<String> allLoginSessionsSuccessfulResponse = credentialManager.tryGetAllLoginSessions(mainCustomerTestCredentials.sessionToken());
            String loginSessionsSuccessBody = allLoginSessionsSuccessfulResponse.body();
            Map loginSessionsSuccessMap = objectMapper.readValue(loginSessionsSuccessBody, Map.class);
            assert loginSessionsSuccessMap.size() == 1;
            Map<String, Object> data = (Map<String, Object>) loginSessionsSuccessMap.get("data");
            assert data.size() == 1;
            List loginSessions = (List) data.get("loginSessions");
            assert !loginSessions.isEmpty();
        } catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }

    private void checkGetLoginSessionsFailure(){
        try {
            HttpResponse<String> allLoginSessionsFailureResponse = credentialManager.tryGetAllLoginSessions(mainCustomerTestCredentials.sessionToken());
            String loginSessionsFailureBody = allLoginSessionsFailureResponse.body();
            Map loginSessionsFailureMap = objectMapper.readValue(loginSessionsFailureBody, Map.class);
            assert loginSessionsFailureMap.size() == 1 && loginSessionsFailureMap.containsKey("errors");
        } catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }
}

