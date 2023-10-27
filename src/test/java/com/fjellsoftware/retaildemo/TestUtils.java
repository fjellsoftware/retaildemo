/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fjellsoftware.javafunctionalutils.opt.Opt;
import com.fjellsoftware.javafunctionalutils.opt.Some;
import com.fjellsoftware.retaildemo.demo.DemoDatabaseResetter;
import com.zaxxer.hikari.HikariDataSource;
import io.loppi.orm.PostgresExecutionException;
import com.fjellsoftware.javafunctionalutils.*;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.TestInfo;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestUtils {

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void cleanup(TestDependencies testDependencies) throws PostgresExecutionException {
        testDependencies.close();
        HikariDataSource dataSource = CoreDependencies.createDataSource(System.getProperty("user.home"));
        OffsetDateTime offsetDateTime2022 = OffsetDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        DemoDatabaseResetter.resetDatabase(dataSource, offsetDateTime2022);
        dataSource.close();
    }

    public static HttpResponse<String> executeGraphQLRequestWithHeadersGetResponse(
            URI applicationURI, String operationString, @Nullable Map<String,Object> variablesNullable,
            Opt<UUID> sessionTokenOpt){
        try {
            String graphQLPostBody = GraphQLUtils.createGraphQLPostBody(operationString, variablesNullable);
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder(applicationURI)
                    .POST(HttpRequest.BodyPublishers.ofString(graphQLPostBody))
                    .headers(TestDependencies.baseHeaders);
            if(sessionTokenOpt instanceof Some<UUID> someToken){
                Cookie cookie = CredentialManager.createSessionCookieFromCredentials(someToken.value());
                httpRequestBuilder.header(cookie.headerName(), cookie.headerValue());
            }
            HttpRequest httpRequest = httpRequestBuilder.build();
            return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static String executeGraphQLRequestWithHeadersGetResultBody(
            URI applicationURI, String operationString, @Nullable Map<String,Object> variablesNullable,
            Opt<UUID> sessionTokeOpt){
        HttpResponse<String> stringHttpResponse = executeGraphQLRequestWithHeadersGetResponse(applicationURI,
                operationString, variablesNullable, sessionTokeOpt);
        return stringHttpResponse.body();
    }

    public static void executeGraphQLRequestWithHeadersAndCheckResult(
            URI applicationURI, String operationString, @Nullable Map<String,Object> variablesNullable,
            Opt<UUID> sessionTokenOpt, Map<String,Object> expectedResultMap){
        NullUtils.requireAllNonNull(applicationURI, operationString, sessionTokenOpt, expectedResultMap);
        String body = executeGraphQLRequestWithHeadersGetResultBody(applicationURI, operationString, variablesNullable, sessionTokenOpt);
        try {
            Map responseMap = objectMapper.readValue(body, Map.class);
            assert responseMap.equals(expectedResultMap);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static final String fullFileContent(InputStream inputStream){
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    public static Map<String, String> graphqlOperationDocumentByOperationName(String allOperations){
        String[] operationDocuments = allOperations.split("####\n");
        Map<String, String> operationsByName = new HashMap<>();
        String queryStart = "query ";
        String mutationStart = "mutation ";
        boolean isFirst = true;
        for (String operationDocument : operationDocuments) {
            if(isFirst){
                isFirst = false;
                continue;
            }
            String operationTypeNameStart;
            if(operationDocument.startsWith(queryStart)){
                operationTypeNameStart = queryStart;
            }
            else if(operationDocument.startsWith(mutationStart)){
                operationTypeNameStart = mutationStart;
            }
            else throw new RuntimeException();
            String substring = operationDocument.substring(operationTypeNameStart.length());
            int endOfOperationName = substring.indexOf(' ');
            String operationName = substring.substring(0, endOfOperationName);
            operationsByName.put(operationName, operationDocument);
        }
        return operationsByName;
    }

    public static String getMethodName(TestInfo testInfo){
        return testInfo.getTestMethod().orElseThrow().getName();
    }

    public static ImmutableMap<String,String> initializeOperationsByName(String resourceFilesFolderName){
        ClassLoader classLoader = TestDependencies.class.getClassLoader();
        InputStream mainQueriesStream = classLoader.getResourceAsStream(Path.of(resourceFilesFolderName, TestDependencies.mainQueriesFileName).toString());
        String fullDocumentString = TestUtils.fullFileContent(mainQueriesStream);
        return new ImmutableMap<>(TestUtils.graphqlOperationDocumentByOperationName(fullDocumentString));
    }

    public static ImmutableMap<String,Map<String,Object>> initializeResults(String resourceFilesFolderName){
        ClassLoader classLoader = TestDependencies.class.getClassLoader();
        InputStream resultsStream = classLoader.getResourceAsStream(Path.of(resourceFilesFolderName, "mainQueriesResults.json").toString());
        String resultsString = TestUtils.fullFileContent(resultsStream);
        try {
            return new ImmutableMap<>(new ObjectMapper().readValue(resultsString, Map.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static URI createTestURI(String relativeURI){
        try {
            // relativeURI example: /api/v1/graphql
            return new URI("http://localhost:8080" + relativeURI);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
