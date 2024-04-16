/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

import io.loppi.orm.PostgresExecutionException;
import com.fjellsoftware.javafunctionalutils.opt.Opt;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RequestSizeTests {
    private final TestDependencies testDependencies;
    private final TestCredentials mainCustomerCredentials;

    public RequestSizeTests() {
        this.testDependencies = new TestDependencies(false);
        CredentialManager credentialManager = new CredentialManager();
        UUID captchaToken = credentialManager.verifyCaptchaOrGetCache();
        mainCustomerCredentials = credentialManager.signUpCustomerAndCheckLoggedIn(captchaToken);
    }

    @AfterAll
    public void cleanup() throws PostgresExecutionException {
        TestUtils.cleanup(testDependencies);
    }

    // A bunch of hardcoded product ids where current price is 2.95, so we don't have to spend a lot of code looking it up.
    int[] productIdsPrice2_95 = new int[]{261, 263, 266, 268, 320, 360, 390, 448, 449, 450, 456, 457, 475, 492, 499, 500, 506,
            532, 564, 565, 578, 634, 637, 656, 657, 658, 716, 719, 723, 724, 725, 726, 754, 757, 758, 759, 761, 762,
            826, 894, 895, 961, 983, 984, 985, 986, 987, 988, 989, 990, 1017, 1035, 1054, 1056, 1058, 1060, 1068, 1069,
            1078, 1104, 1109, 1110, 1113, 1151, 1152, 1153, 1154, 1190, 1196, 1204, 1205, 1206, 1234, 1235, 1236, 1237};
    @Test
    public void placeOrderSize(){
        Map<String,Object> variables = new HashMap<>();
        variables.put("address", CustomerApiTests.defaultAddress);
        variables.put("name", CustomerApiTests.defaultName);
        variables.put("phoneNumber", CustomerApiTests.defaultPhoneNumber);
        variables.put("countryId", 9);
        List<Map<String,Object>> orderLinesList = new ArrayList<>();
        for (int productId : productIdsPrice2_95) {
            orderLinesList.add(CustomerApiTests.createOrderLineMap(productId, 1, "2.95"));
        }
        variables.put("orderLines", orderLinesList);
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(CustomerApiTests.applicationURI,
                CustomerApiTests.fullDocumentStringByOperationName.get(CustomerApiTests.placeOrderOperationName).getOrThrow(),
                variables, Opt.of(mainCustomerCredentials.sessionToken()), CustomerApiTests.resultsMap.get("mutationTooBig").getOrThrow());
    }

    @Test
    public void querySize(){
        Map<String,Object> variables = new HashMap<>();
        variables.put("ordersLimit", 20);
        variables.put("orderLinesLimit", 20);
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(CustomerApiTests.applicationURI,
                CustomerApiTests.fullDocumentStringByOperationName.get("getOrdersVariableLimits").getOrThrow(),
                variables, Opt.of(mainCustomerCredentials.sessionToken()), CustomerApiTests.resultsMap.get("queryTooBig").getOrThrow());
    }

    @Test
    public void complicatedIntrospection(){
        TestUtils.executeGraphQLRequestWithHeadersAndCheckResult(CustomerApiTests.applicationURI,
                CustomerApiTests.fullDocumentStringByOperationName.get("introspectionTooDeep").getOrThrow(),
                null, Opt.empty(), CustomerApiTests.resultsMap.get("queryTooDeep").getOrThrow());
    }
}
