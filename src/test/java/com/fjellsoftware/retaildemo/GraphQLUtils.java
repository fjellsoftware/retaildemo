/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fjellsoftware.javafunctionalutils.ImmutableMap;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GraphQLUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String createGraphQLPostBody(
            String fullOperation, @Nullable Map<String, Object> variablesNullable){
        Map<String,Object> graphQLRequestBody = new HashMap<>();
        graphQLRequestBody.put("query", fullOperation);
        if(variablesNullable != null){
            graphQLRequestBody.put("variables", variablesNullable);
        }
        try {
            return objectMapper.writeValueAsString(graphQLRequestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String createGraphQLPostBody(
            ImmutableMap<String, String> fullDocumentStringByOperationName, String operationName,
            @Nullable Map<String, Object> variablesNullable){
        return createGraphQLPostBody(fullDocumentStringByOperationName.get(operationName).getOrThrow(), variablesNullable);
    }
}
