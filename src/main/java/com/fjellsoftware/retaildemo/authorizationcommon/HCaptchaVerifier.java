/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.authorizationcommon;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.loppi.graphql.GraphQLExecutableMutationField;
import io.loppi.graphql.GraphQLRequestException;
import io.loppi.graphql.schema.GraphQLErrorTypeCategory;
import com.fjellsoftware.retaildemo.ApplicationInternalException;
import com.fjellsoftware.retaildemo.authorizationcommon.cookie.CookieModification;
import com.fjellsoftware.retaildemo.authorizationcommon.cookie.CookieToAdd;
import com.fjellsoftware.retaildemo.autogenerated.graphql.customer.CustomerGraphQLService;
import com.fjellsoftware.retaildemo.autogenerated.graphql.customer.VerifyCaptchaMutationField;
import com.fjellsoftware.retaildemo.autogenerated.graphql.customer.VerifyCaptchaResult;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HCaptchaVerifier {

    private final HCaptchaCache hCaptchaCache;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String secret;
    private final String siteKey;
    private final URI verifyURI;
    private final ObjectMapper objectMapper;

    private final CustomerGraphQLService customerGraphQLService;

    public static final String H_CAPTCHA_TOKEN_COOKIE_NAME = "hCaptchaToken";

    public HCaptchaVerifier(String secret, String siteKey, CustomerGraphQLService customerGraphQLService,
                            HCaptchaCache hCaptchaCache){
        this.hCaptchaCache = hCaptchaCache;
        this.secret = secret;
        this.siteKey = siteKey;
        try {
            this.verifyURI = new URI("https://hcaptcha.com/siteverify");
        } catch (URISyntaxException e) {
            throw new ApplicationInternalException("Failed to initialize HCaptchaVerifier due to invalid URI.", e);
        }
        this.objectMapper = new ObjectMapper();

        this.customerGraphQLService = customerGraphQLService;
    }

    public MutationFieldAndCookies verifyHCaptchaTokenMutation(
            VerifyCaptchaMutationField verifyCaptchaField) throws GraphQLRequestException {
        String responseToken = verifyCaptchaField.input().token();
        if(responseToken.length() > 5_000){
            throw new GraphQLRequestException("Invalid captcha token.", GraphQLErrorTypeCategory.OTHER);
        }
        String requestBody = String.format("response=%s&secret=%s&sitekey=%s", responseToken, secret, siteKey);
        HttpRequest request = HttpRequest.newBuilder(verifyURI)
                .header("content-type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw GraphQLRequestException.createInternal(e);
        } catch (InterruptedException e) {
            throw new ApplicationInternalException("Unexpected thread interruption while verifying captcha.", e);
        }
        boolean success;
        try {
            Map responseBodyMap = objectMapper.readValue(response.body(), HashMap.class);
            success = (boolean) responseBodyMap.get("success");
        } catch (Exception e) {
            throw new ApplicationInternalException("Unexpected error while parsing captcha response.", e);
        }
        if(!success){
            throw new GraphQLRequestException("Invalid captcha response.", GraphQLErrorTypeCategory.OTHER);
        }
        UUID token = UUID.randomUUID();
        String tokenString = token.toString();
        GraphQLExecutableMutationField executableMutationField = customerGraphQLService.createExecutableCustomMutationField(
                List.of(), new VerifyCaptchaResult(token), verifyCaptchaField);
        hCaptchaCache.registerValidToken(tokenString);
        List<CookieModification> cookiesToModify = List.of(new CookieToAdd(H_CAPTCHA_TOKEN_COOKIE_NAME, tokenString));
        return new MutationFieldAndCookies(executableMutationField, cookiesToModify);
    }
}
