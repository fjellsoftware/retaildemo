/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.autogenerated.graphql.staff;

import io.loppi.graphql.GraphQLDatabaseQueryField;


/////////////////////
/// AUTOGENERATED ///
/////////////////////


public sealed interface StaffDatabaseQueryRootField extends StaffQueryRootField
         permits LoginSessionsQueryField, OrdersQueryField, UsersQueryField {
    GraphQLDatabaseQueryField<?> queryField();
}
