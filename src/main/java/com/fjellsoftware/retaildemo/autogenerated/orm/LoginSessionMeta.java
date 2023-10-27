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

public final class LoginSessionMeta{
    private static final Class<LoginSessionResult> referenceClass = LoginSessionResult.class;

    private static final String LOGIN_SESSION_ID_FIELD_NAME = "loginSessionId";
    private static final String IS_SIGNED_OUT_FIELD_NAME = "isSignedOut";
    private static final String CREATED_AT_FIELD_NAME = "createdAt";
    private static final String LAST_UPDATED_AT_FIELD_NAME = "lastUpdatedAt";
    private static final String USER_ACCOUNT_FIELD_NAME = "userAccount";
    private static final String USER_ACCOUNT_ID_FIELD_NAME = "userAccountId";

    static final ScalarAttributeReference<LoginSessionResult, IncludableUUID> loginSessionIdInternalReference = 
        new ScalarAttributeReference<>(referenceClass, LOGIN_SESSION_ID_FIELD_NAME, IncludableUUID.class);
    static final OwnerAssociationAttributeReference<LoginSessionResult, IncludableAssociation<UserAccountResult>, UserAccountResult> userAccountInternalReference = 
        new OwnerAssociationAttributeReference<>(referenceClass, USER_ACCOUNT_FIELD_NAME, false, UserAccountResult.class);

    public final ScalarAttribute<LoginSessionResult, IncludableUUID> loginSessionId;
    public final ScalarAttribute<LoginSessionResult, IncludableBoolean> isSignedOut;
    public final ScalarAttribute<LoginSessionResult, IncludableOffsetDateTime> createdAt;
    public final ScalarAttribute<LoginSessionResult, IncludableOffsetDateTime> lastUpdatedAt;
    public final OwnerAssociationAttribute<LoginSessionResult, IncludableAssociation<UserAccountResult>, UserAccountResult> userAccount;
    public final ScalarAttribute<LoginSessionResult, IncludableUUID> userAccountId;

    private final EntityMetadata<LoginSessionResult> entityMetadata;

    @SuppressWarnings({"unchecked"})
    public LoginSessionMeta(LoppiMetamodel<RetailDemoDatabaseQueryResult> loppiMetamodel){
        this.entityMetadata = loppiMetamodel.getEntityMetadata(referenceClass).getOrThrow();
        this.loginSessionId = (ScalarAttribute<LoginSessionResult, IncludableUUID>) entityMetadata.tryGetScalarAttribute(LOGIN_SESSION_ID_FIELD_NAME).getOrThrow();
        this.isSignedOut = (ScalarAttribute<LoginSessionResult, IncludableBoolean>) entityMetadata.tryGetScalarAttribute(IS_SIGNED_OUT_FIELD_NAME).getOrThrow();
        this.createdAt = (ScalarAttribute<LoginSessionResult, IncludableOffsetDateTime>) entityMetadata.tryGetScalarAttribute(CREATED_AT_FIELD_NAME).getOrThrow();
        this.lastUpdatedAt = (ScalarAttribute<LoginSessionResult, IncludableOffsetDateTime>) entityMetadata.tryGetScalarAttribute(LAST_UPDATED_AT_FIELD_NAME).getOrThrow();
        this.userAccount = (OwnerAssociationAttribute<LoginSessionResult, IncludableAssociation<UserAccountResult>, UserAccountResult>) entityMetadata.tryGetAssociationAttribute(USER_ACCOUNT_FIELD_NAME).getOrThrow();
        this.userAccountId = (ScalarAttribute<LoginSessionResult, IncludableUUID>) entityMetadata.tryGetScalarAttribute(USER_ACCOUNT_ID_FIELD_NAME).getOrThrow();
    }

    public EntityMetadata<LoginSessionResult> getEntityMetadata(){
        return entityMetadata;
    }
}