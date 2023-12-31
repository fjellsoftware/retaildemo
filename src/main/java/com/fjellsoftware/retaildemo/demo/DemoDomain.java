/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.demo;

import com.fjellsoftware.retaildemo.autogenerated.graphql.customer.*;
import io.loppi.graphql.GraphQLExecutableMutationField;
import io.loppi.graphql.GraphQLRequestException;
import io.loppi.graphql.IgnoredResult;
import io.loppi.graphql.schema.GraphQLFieldDefinition;
import com.fjellsoftware.retaildemo.authorizationcommon.MutationFieldAndCookies;
import com.fjellsoftware.retaildemo.authorizationcommon.userinfo.AuthenticatedUser;
import com.fjellsoftware.retaildemo.authorizationcommon.LoginSessionService;
import com.fjellsoftware.retaildemo.authorizationcommon.RoleEnum;
import com.fjellsoftware.retaildemo.authorizationcommon.userinfo.UserInfo;
import com.fjellsoftware.retaildemo.autogenerated.orm.UserAccountToUpdate;

import java.util.Collections;
import java.util.List;

public class DemoDomain {

    private final LoginSessionService loginSessionService;
    private final CustomerGraphQLService customerGraphQLService;

    public DemoDomain(LoginSessionService loginSessionService, CustomerGraphQLService customerGraphQLService) {
        this.loginSessionService = loginSessionService;
        this.customerGraphQLService = customerGraphQLService;
    }

    public static GraphQLFieldDefinition upgradeToStaffCustomMutationDefinition(){
        return GraphQLFieldDefinition.builder().setName("demoUpgradeToStaff").setNotNull(true)
                .setDescription("Upgrades current user to a staff member role. Anyone who is logged in can do this. " +
                        "Don't do this in production without access control.")
                .setType(IgnoredResult.getGraphQLObjectType()).build();
    }

    public static GraphQLFieldDefinition downGradeToCustomerCustomMutationDefiniton(){
        return GraphQLFieldDefinition.builder().setName("demoDowngradeToCustomer").setNotNull(true)
                .setDescription("Downgrades current user to customer role.")
                .setType(IgnoredResult.getGraphQLObjectType()).build();
    }

    public MutationFieldAndCookies applyUpgradeToStaff(
            UserInfo userInfo, DemoUpgradeToStaffMutationField mutationField) throws GraphQLRequestException {
        UserAccountToUpdate upgradeUpdate = updateRoleCommon(userInfo, RoleEnum.STAFF);
        GraphQLExecutableMutationField executableMutationField = customerGraphQLService.createExecutableCustomMutationField(
                List.of(upgradeUpdate), new DemoUpgradeToStaffResult(true), mutationField);
        return new MutationFieldAndCookies(executableMutationField, Collections.emptyList());
    }

    public MutationFieldAndCookies applyDowngradeToCustomer(
            UserInfo userInfo, DemoDowngradeToCustomerMutationField mutationField) throws GraphQLRequestException{
        UserAccountToUpdate downgradeUpdate = updateRoleCommon(userInfo, RoleEnum.CUSTOMER);
        GraphQLExecutableMutationField executableMutationField = customerGraphQLService.createExecutableCustomMutationField(
                List.of(downgradeUpdate), new DemoDowngradeToCustomerResult(true), mutationField);
        return new MutationFieldAndCookies(executableMutationField, Collections.emptyList());
    }

    private UserAccountToUpdate updateRoleCommon(
            UserInfo userInfo, RoleEnum updatedRole) throws GraphQLRequestException{
        if(!(userInfo instanceof AuthenticatedUser authenticatedUser)){
            throw GraphQLRequestException.createNotLoggedIn();
        }
        loginSessionService.invalidateSession(authenticatedUser.sessionToken());
        UserAccountToUpdate updatedAccount = new UserAccountToUpdate(authenticatedUser.userId());
        updatedAccount.setRoleIncludableOf(updatedRole.name());
        return updatedAccount;
    }
}
