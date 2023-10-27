/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.authorizationcommon;

import com.fjellsoftware.retaildemo.authorizationcommon.cookie.CookieModification;
import io.loppi.graphql.GraphQLExecutableMutationField;

import java.util.List;

public record MutationFieldAndCookies(GraphQLExecutableMutationField mutationField, List<CookieModification> cookiesToModify) {
}
