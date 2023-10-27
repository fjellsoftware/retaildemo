/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.loppi.orm.PostgresExecutionException;
import com.fjellsoftware.javafunctionalutils.opt.Opt;
import org.junit.jupiter.api.*;

import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RateLimitTests {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TestCredentials mainCustomerCredentials;

    private TestDependencies testDependencies;

    public RateLimitTests() {
        TestDependencies testDependencies = new TestDependencies(true);
        CredentialManager credentialManager = new CredentialManager();
        UUID captchaToken = credentialManager.verifyCaptchaOrGetCache();
        mainCustomerCredentials = credentialManager.signUpCustomerAndCheckLogin(captchaToken);
        testDependencies.close();
    }

    @AfterAll
    public void cleanup() throws PostgresExecutionException {
        TestUtils.cleanup(testDependencies);
    }

    @BeforeEach
    public void startServerCleanRateLimit(){
        testDependencies = new TestDependencies(true);
    }

    @AfterEach
    public void closeServer() {
        testDependencies.close();
    }

    private static final String rateLimitResultName = "rateLimit";
    @Test
    public void queriesRateLimit() throws JsonProcessingException {
        int subsequentRequestsToTry = 100;
        boolean hitRateLimit = false;
        for (int i = 0; i < subsequentRequestsToTry; i++) {
            String body = TestUtils.executeGraphQLRequestWithHeadersGetResultBody(CustomerApiTests.applicationURI,
                    CustomerApiTests.fullDocumentStringByOperationName.get(CustomerApiTests.getLatestProductsOperationName).getOrThrow(),
                    null, Opt.empty());
            Map resultMap = objectMapper.readValue(body, Map.class);
            if(resultMap.equals(CustomerApiTests.resultsMap.get(rateLimitResultName).getOrThrow())){
                hitRateLimit = true;
                break;
            }
        }
        assert hitRateLimit;
    }

    @Test
    public void mutationsRateLimit() throws JsonProcessingException {
        int subsequentRequestsToTry = 20;
        boolean hitRateLimit = false;
        for (int i = 0; i < subsequentRequestsToTry; i++) {
            Map<String, Object> placeOrderVariables = CustomerApiTests.createPlaceOrderVariables(mainCustomerCredentials.userAccountId(), true);
            String body = TestUtils.executeGraphQLRequestWithHeadersGetResultBody(CustomerApiTests.applicationURI,
                    CustomerApiTests.fullDocumentStringByOperationName.get(CustomerApiTests.placeOrderOperationName).getOrThrow(),
                    placeOrderVariables, Opt.of(mainCustomerCredentials.sessionToken()));
            Map resultMap = objectMapper.readValue(body, Map.class);
            if(resultMap.equals(CustomerApiTests.resultsMap.get(rateLimitResultName).getOrThrow())){
                hitRateLimit = true;
                break;
            }
        }
        assert hitRateLimit;
    }

    @Test
    public void loginRateLimit() throws Exception {
        int subsequentRequestsToTry = 10;
        boolean hitRateLimit = false;

        CredentialManager credentialManager = new CredentialManager();
        UUID hCaptchaToken = credentialManager.verifyCaptchaOrGetCache();
        String hCaptchaCookieRequestHeaderValue = CredentialManager.createHCaptchaCookieValue(hCaptchaToken);
        String username = "spasdoseqw";
        String salt = credentialManager.executeGetSalt(username);
        for (int i = 0; i < subsequentRequestsToTry; i++) {
            HttpResponse<String> httpResponse = credentialManager.loginWithSalt(
                    username, "spasdoseqwsfasawd", hCaptchaCookieRequestHeaderValue, salt);
            String body = httpResponse.body();
            Map resultMap = objectMapper.readValue(body, Map.class);
            if(resultMap.equals(CustomerApiTests.resultsMap.get(rateLimitResultName).getOrThrow())){
                hitRateLimit = true;
                break;
            }
        }
        assert hitRateLimit;
    }
}
