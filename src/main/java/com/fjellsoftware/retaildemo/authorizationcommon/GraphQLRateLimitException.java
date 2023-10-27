/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.authorizationcommon;


import io.loppi.graphql.GraphQLRequestException;
import io.loppi.graphql.schema.GraphQLErrorTypeCategory;

public class GraphQLRateLimitException extends GraphQLRequestException {
    public GraphQLRateLimitException() {
        super("Too many requests, slow down.", GraphQLErrorTypeCategory.OTHER);
    }
}
