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

import java.net.URI;
import java.util.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StaffApiTests {

    public static final URI applicationURI = TestUtils.createTestURI("/api/v1/staff/graphql");
    private static final String resourceFilesFolderName = "staffApi";
    public static final ImmutableMap<String, String> fullDocumentStringByOperationName =
            TestUtils.initializeOperationsByName(resourceFilesFolderName);
    public static final ImmutableMap<String, Map<String,Object>> resultsMap =
            TestUtils.initializeResults(resourceFilesFolderName);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TestDependencies testDependencies;
    private final CredentialManager credentialManager;
    private final TestCredentials mainStaffCredentials;
    
    public StaffApiTests() {
        this.testDependencies = new TestDependencies(false);
        this.credentialManager = new CredentialManager();
        UUID captchaToken = credentialManager.verifyCaptchaOrGetCache();
        TestCredentials customerCredentials = credentialManager.signUpCustomerAndCheckLoggedIn(captchaToken);
        credentialManager.tryUpgradeToStaff(customerCredentials.sessionToken());
        mainStaffCredentials = customerCredentials;
    }

    @AfterAll
    public void cleanup() throws PostgresExecutionException {
        TestUtils.cleanup(testDependencies);
    }

    public static final String getOrdersOperationName = "getOrders";
    @Test
    public void _1_getOrders(){
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get(getOrdersOperationName).getOrThrow(), null, Opt.of(mainStaffCredentials.sessionToken()),
                resultsMap.get(getOrdersOperationName).getOrThrow());
    }

    private static final String notLoggedIn = "notLoggedIn";
    private static final String unautorized = "unauthorized";
    @Test
    public void getOrdersUnauthenticated(){
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get(getOrdersOperationName).getOrThrow(), null, Opt.empty(),
                resultsMap.get(notLoggedIn).getOrThrow());
    }

    @Test
    public void zz_getOrdersFromCustomer(){
        credentialManager.tryDowngradeToCustomer(mainStaffCredentials.sessionToken());
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get(getOrdersOperationName).getOrThrow(), null, Opt.of(mainStaffCredentials.sessionToken()),
                resultsMap.get(unautorized).getOrThrow());
    }

    private static final String getLoginSessionsOperationName = "getLoginSessions";
    @Test
    public void getLoginSessions(){
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get(getLoginSessionsOperationName).getOrThrow(), null,
                Opt.of(mainStaffCredentials.sessionToken()), resultsMap.get(getLoginSessionsOperationName).getOrThrow());
    }

    @Test
    public void getLoginSessionsUnauthenticated(){
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get(getLoginSessionsOperationName).getOrThrow(), null, Opt.empty(),
                resultsMap.get(notLoggedIn).getOrThrow());
    }

    @Test
    public void zz_getLoginSessionsFromCustomer(){
        credentialManager.tryDowngradeToCustomer(mainStaffCredentials.sessionToken());
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get(getLoginSessionsOperationName).getOrThrow(), null, Opt.of(mainStaffCredentials.sessionToken()),
                resultsMap.get(unautorized).getOrThrow());
    }

    private static final String getUsersOperationName = "getUsers";
    @Test
    public void getUsers(){
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get(getUsersOperationName).getOrThrow(), null, Opt.of(mainStaffCredentials.sessionToken()),
                resultsMap.get(getUsersOperationName).getOrThrow());
    }

    @Test
    public void zz_getUsersFromCustomer(){
        credentialManager.tryDowngradeToCustomer(mainStaffCredentials.sessionToken());
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get(getUsersOperationName).getOrThrow(), null, Opt.of(mainStaffCredentials.sessionToken()),
                resultsMap.get(unautorized).getOrThrow());
    }

    @Test
    public void getUsersUnauthenticated(){
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get(getUsersOperationName).getOrThrow(), null, Opt.empty(),
                resultsMap.get(notLoggedIn).getOrThrow());
    }

    private static final String getOrderStatusesOperationName = "getOrderStatuses";
    @Test
    public void orderStatusBatchUpdate() throws JsonProcessingException {
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get(getOrderStatusesOperationName).getOrThrow(), null,
                Opt.of(mainStaffCredentials.sessionToken()), resultsMap.get(getOrderStatusesOperationName).getOrThrow());
        Map<String, Object> placeOrderVariables = CustomerApiTests.createPlaceOrderVariables(mainStaffCredentials.userAccountId(), true);
        String placeOrderBody = TestUtils.executeGraphQLRequestWithHeadersGetResultBody(CustomerApiTests.applicationURI,
                CustomerApiTests.fullDocumentStringByOperationName.get("placeOrder").getOrThrow(), placeOrderVariables, Opt.of(mainStaffCredentials.sessionToken()));
        Map placeOrderResultMap = objectMapper.readValue(placeOrderBody, Map.class);
        assert placeOrderResultMap.size() == 1;
        Map data = (Map) placeOrderResultMap.get("data");
        Map placeOrder = (Map) data.get("placeOrder");
        int purchaseOrderId = (int) placeOrder.get("purchaseOrderId");

        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get(getOrderStatusesOperationName).getOrThrow(), null,
                Opt.of(mainStaffCredentials.sessionToken()), resultsMap.get(getOrderStatusesOperationName+"New").getOrThrow());

        Map<String,Object> updateOrderStatusesVariables = new HashMap<>();
        updateOrderStatusesVariables.put("purchaseOrderId", purchaseOrderId);
        String updateOrderStatusesBody = TestUtils.executeGraphQLRequestWithHeadersGetResultBody(applicationURI,
                fullDocumentStringByOperationName.get("updateOrderStatuses").getOrThrow(), updateOrderStatusesVariables, Opt.of(mainStaffCredentials.sessionToken()));
        Map updateOrderResultMap = objectMapper.readValue(updateOrderStatusesBody, Map.class);
        assert updateOrderResultMap.size() == 1 && updateOrderResultMap.containsKey("data");

        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get(getOrderStatusesOperationName).getOrThrow(), null,
                Opt.of(mainStaffCredentials.sessionToken()), resultsMap.get(getOrderStatusesOperationName).getOrThrow());
    }

    private static final String productInsertOperationName = "productInsert";
    @Test
    public void _1_productInsert() throws JsonProcessingException {
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(CustomerApiTests.applicationURI,
                CustomerApiTests.fullDocumentStringByOperationName.get(CustomerApiTests.getLatestProductsOperationName).getOrThrow(),
                null, Opt.empty(),
                CustomerApiTests.resultsMap.get(CustomerApiTests.getLatestProductsOperationName).getOrThrow());

        Map<String,Object> productInsertVariables = new HashMap<>();
        Map<String,Object> productToInsert = new HashMap<>();
        productToInsert.put("currentUnitPrice", "1.45");
        productToInsert.put("description", "LARGE INFLATABLE ANIMAL");
        productInsertVariables.put("productToInsert", productToInsert);
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get(productInsertOperationName).getOrThrow(), productInsertVariables,
                Opt.of(mainStaffCredentials.sessionToken()), resultsMap.get(productInsertOperationName).getOrThrow());

        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(CustomerApiTests.applicationURI,
                CustomerApiTests.fullDocumentStringByOperationName.get(CustomerApiTests.getLatestProductsOperationName).getOrThrow(),
                null, Opt.empty(),
                resultsMap.get("latestProductsWithInflatableAnimal").getOrThrow());

        String body = TestUtils.executeGraphQLRequestWithHeadersGetResultBody(CustomerApiTests.applicationURI,
                CustomerApiTests.fullDocumentStringByOperationName.get("getLatestProductId").getOrThrow(),
                null, Opt.empty());
        Map resultMap = objectMapper.readValue(body, Map.class);
        List products = (List) ((Map) resultMap.get("data")).get("products");
        int productId = (int) ((Map) products.get(0)).get("productId");
        productIdFromInsert = productId;
    }

    private int productIdFromInsert = 0;
    private static final String productUpdateOperationName = "productUpdate";
    @Test
    public void _2_productUpdate() throws JsonProcessingException {
        if(productIdFromInsert == 0){
            _1_productInsert();
        }

        Map<String,Object> updateProductVariables = new HashMap<>();
        updateProductVariables.put("productId", productIdFromInsert);
        updateProductVariables.put("description", "SMALL INFLATABLE ANIMAL");
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
                fullDocumentStringByOperationName.get(productUpdateOperationName).getOrThrow(), updateProductVariables,
                Opt.of(mainStaffCredentials.sessionToken()), resultsMap.get(productUpdateOperationName).getOrThrow());
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(CustomerApiTests.applicationURI,
                CustomerApiTests.fullDocumentStringByOperationName.get(CustomerApiTests.getLatestProductsOperationName).getOrThrow(),
                null, Opt.empty(),
                resultsMap.get("latestProductsAfterUpdate").getOrThrow());
    }


//    ####
//    mutation productDelete ($productId: Int!){
//        productDelete(productToDelete: { whereProductId: $productId}){
//            description
//        }
//    }

//    "productDelete": {
//        "data": {
//            "productDelete": {
//                "description": "SMALL INFLATABLE ANIMAL"
//            }
//        }
//    },

//    private static final String productDeleteOperationName = "productDelete";
//    @Test
//    public void _3_productDelete() throws JsonProcessingException {
//        if(productIdFromInsert == 0){
//            _2_productUpdate();
//        }
//
//        Map<String,Object> deleteProductVariables = new HashMap<>();
//        deleteProductVariables.put("productId", productIdFromInsert);
//        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(applicationURI,
//                fullDocumentStringByOperationName.get(productDeleteOperationName), deleteProductVariables,
//                Opt.of(mainStaffCredentials), resultsMap.get(productDeleteOperationName));
//        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(CustomerApiTests.applicationURI,
//                CustomerApiTests.fullDocumentStringByOperationName.get(CustomerApiTests.getLatestProductsOperationName),
//                null, Opt.empty(),
//                CustomerApiTests.resultsMap.get(CustomerApiTests.getLatestProductsOperationName));
//    }
}
