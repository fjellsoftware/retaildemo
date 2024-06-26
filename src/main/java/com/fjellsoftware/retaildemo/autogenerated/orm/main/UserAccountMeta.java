package com.fjellsoftware.retaildemo.autogenerated.orm.main;

import io.loppi.orm.includablevalues.*;
import io.loppi.orm.metamodel.*;
import io.loppi.orm.metamodel.attribute.*;
import io.loppi.orm.metamodel.attributereference.*;
import com.fjellsoftware.javafunctionalutils.ImmutableMap;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public final class UserAccountMeta implements MetaWithIdAttribute<UserAccountResult> {
    private static final Class<UserAccountResult> referenceClass = UserAccountResult.class;

    static final String BASE_NAME = "UserAccount";
    static final String PLURAL_NAME = "UserAccounts";
    static final String TABLE_NAME = "user_account";

    private static final String NAME_FIELD_NAME = "name";
    private static final String USERNAME_FIELD_NAME = "username";
    private static final String HASHED_PASSWORD_FIELD_NAME = "hashedPassword";
    private static final String CREATED_AT_FIELD_NAME = "createdAt";
    private static final String LAST_UPDATED_AT_FIELD_NAME = "lastUpdatedAt";
    private static final String ROLE_FIELD_NAME = "role";
    private static final String USER_ACCOUNT_ID_FIELD_NAME = "userAccountId";
    private static final String LOGIN_SESSIONS_FIELD_NAME = "loginSessions";
    private static final String PURCHASE_ORDERS_FIELD_NAME = "purchaseOrders";

    static final ImmutableMap<String,String> COLUMN_ATTRIBUTE_NAME_TO_COLUMN_NAME = ImmutableMap.of(
            new ImmutableMap.Entry<>(NAME_FIELD_NAME, "name"),
            new ImmutableMap.Entry<>(USERNAME_FIELD_NAME, "username"),
            new ImmutableMap.Entry<>(HASHED_PASSWORD_FIELD_NAME, "hashed_password"),
            new ImmutableMap.Entry<>(CREATED_AT_FIELD_NAME, "created_at"),
            new ImmutableMap.Entry<>(LAST_UPDATED_AT_FIELD_NAME, "last_updated_at"),
            new ImmutableMap.Entry<>(ROLE_FIELD_NAME, "role"),
            new ImmutableMap.Entry<>(USER_ACCOUNT_ID_FIELD_NAME, "user_account_id"));

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

    private final EntityMetadata<UserAccountResult> entity_metadata;

    @SuppressWarnings({"unchecked"})
    public UserAccountMeta(ORMMetamodel<RetailDemoTableQueryResult> oRMMetamodel){
        this.entity_metadata = oRMMetamodel.getEntityMetadata(referenceClass);
        this.name = (ScalarAttribute<UserAccountResult, IncludableString>) entity_metadata.getScalarAttributeByName(NAME_FIELD_NAME).getOrThrow();
        this.username = (ScalarAttribute<UserAccountResult, IncludableString>) entity_metadata.getScalarAttributeByName(USERNAME_FIELD_NAME).getOrThrow();
        this.hashedPassword = (ScalarAttribute<UserAccountResult, IncludableString>) entity_metadata.getScalarAttributeByName(HASHED_PASSWORD_FIELD_NAME).getOrThrow();
        this.createdAt = (ScalarAttribute<UserAccountResult, IncludableOffsetDateTime>) entity_metadata.getScalarAttributeByName(CREATED_AT_FIELD_NAME).getOrThrow();
        this.lastUpdatedAt = (ScalarAttribute<UserAccountResult, IncludableOffsetDateTime>) entity_metadata.getScalarAttributeByName(LAST_UPDATED_AT_FIELD_NAME).getOrThrow();
        this.role = (ScalarAttribute<UserAccountResult, IncludableString>) entity_metadata.getScalarAttributeByName(ROLE_FIELD_NAME).getOrThrow();
        this.userAccountId = (ScalarAttribute<UserAccountResult, IncludableUUID>) entity_metadata.getScalarAttributeByName(USER_ACCOUNT_ID_FIELD_NAME).getOrThrow();
        this.loginSessions = (PluralDerivedAssociationAttribute<UserAccountResult, LoginSessionResult>) entity_metadata.getAssociationAttributeByName(LOGIN_SESSIONS_FIELD_NAME).getOrThrow();
        this.purchaseOrders = (PluralDerivedAssociationAttribute<UserAccountResult, PurchaseOrderResult>) entity_metadata.getAssociationAttributeByName(PURCHASE_ORDERS_FIELD_NAME).getOrThrow();
    }

    public EntityMetadata<UserAccountResult> getEntityMetadata(){
        return entity_metadata;
    }
}