/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.autogenerated.orm;

import io.loppi.includablevalues.*;
import io.loppi.orm.metamodel.*;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public final class UserAccountMeta{
    private static final Class<UserAccountResult> referenceClass = UserAccountResult.class;

    private static final String NAME_FIELD_NAME = "name";
    private static final String USERNAME_FIELD_NAME = "username";
    private static final String HASHED_PASSWORD_FIELD_NAME = "hashedPassword";
    private static final String CREATED_AT_FIELD_NAME = "createdAt";
    private static final String LAST_UPDATED_AT_FIELD_NAME = "lastUpdatedAt";
    private static final String ROLE_FIELD_NAME = "role";
    private static final String USER_ACCOUNT_ID_FIELD_NAME = "userAccountId";
    private static final String LOGIN_SESSIONS_FIELD_NAME = "loginSessions";
    private static final String PURCHASE_ORDERS_FIELD_NAME = "purchaseOrders";

    static final ScalarAttributeReference<UserAccountResult, IncludableUUID> userAccountIdInternalReference = 
        new ScalarAttributeReference<>(referenceClass, USER_ACCOUNT_ID_FIELD_NAME, IncludableUUID.class);
    static final PluralDerivedAssociationAttributeReference<UserAccountResult, LoginSessionResult> loginSessionsInternalReference = 
        new PluralDerivedAssociationAttributeReference<>(referenceClass, LOGIN_SESSIONS_FIELD_NAME, LoginSessionResult.class);
    static final PluralDerivedAssociationAttributeReference<UserAccountResult, PurchaseOrderResult> purchaseOrdersInternalReference = 
        new PluralDerivedAssociationAttributeReference<>(referenceClass, PURCHASE_ORDERS_FIELD_NAME, PurchaseOrderResult.class);

    public final ScalarAttribute<UserAccountResult, IncludableString> name;
    public final ScalarAttribute<UserAccountResult, IncludableString> username;
    public final ScalarAttribute<UserAccountResult, IncludableString> hashedPassword;
    public final ScalarAttribute<UserAccountResult, IncludableOffsetDateTime> createdAt;
    public final ScalarAttribute<UserAccountResult, IncludableOffsetDateTime> lastUpdatedAt;
    public final ScalarAttribute<UserAccountResult, IncludableString> role;
    public final ScalarAttribute<UserAccountResult, IncludableUUID> userAccountId;
    public final PluralDerivedAssociationAttribute<UserAccountResult, LoginSessionResult> loginSessions;
    public final PluralDerivedAssociationAttribute<UserAccountResult, PurchaseOrderResult> purchaseOrders;

    private final EntityMetadata<UserAccountResult> entityMetadata;

    @SuppressWarnings({"unchecked"})
    public UserAccountMeta(LoppiMetamodel<RetailDemoDatabaseQueryResult> loppiMetamodel){
        this.entityMetadata = loppiMetamodel.getEntityMetadata(referenceClass).getOrThrow();
        this.name = (ScalarAttribute<UserAccountResult, IncludableString>) entityMetadata.tryGetScalarAttribute(NAME_FIELD_NAME).getOrThrow();
        this.username = (ScalarAttribute<UserAccountResult, IncludableString>) entityMetadata.tryGetScalarAttribute(USERNAME_FIELD_NAME).getOrThrow();
        this.hashedPassword = (ScalarAttribute<UserAccountResult, IncludableString>) entityMetadata.tryGetScalarAttribute(HASHED_PASSWORD_FIELD_NAME).getOrThrow();
        this.createdAt = (ScalarAttribute<UserAccountResult, IncludableOffsetDateTime>) entityMetadata.tryGetScalarAttribute(CREATED_AT_FIELD_NAME).getOrThrow();
        this.lastUpdatedAt = (ScalarAttribute<UserAccountResult, IncludableOffsetDateTime>) entityMetadata.tryGetScalarAttribute(LAST_UPDATED_AT_FIELD_NAME).getOrThrow();
        this.role = (ScalarAttribute<UserAccountResult, IncludableString>) entityMetadata.tryGetScalarAttribute(ROLE_FIELD_NAME).getOrThrow();
        this.userAccountId = (ScalarAttribute<UserAccountResult, IncludableUUID>) entityMetadata.tryGetScalarAttribute(USER_ACCOUNT_ID_FIELD_NAME).getOrThrow();
        this.loginSessions = (PluralDerivedAssociationAttribute<UserAccountResult, LoginSessionResult>) entityMetadata.tryGetAssociationAttribute(LOGIN_SESSIONS_FIELD_NAME).getOrThrow();
        this.purchaseOrders = (PluralDerivedAssociationAttribute<UserAccountResult, PurchaseOrderResult>) entityMetadata.tryGetAssociationAttribute(PURCHASE_ORDERS_FIELD_NAME).getOrThrow();
    }

    public EntityMetadata<UserAccountResult> getEntityMetadata(){
        return entityMetadata;
    }
}
