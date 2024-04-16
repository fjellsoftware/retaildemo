package com.fjellsoftware.retaildemo.autogenerated.orm;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

import java.beans.ConstructorProperties;
import com.fjellsoftware.javafunctionalutils.NullUtils;
import io.loppi.orm.includablevalues.*;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public final class LoginSessionToInsert implements LoginSessionReference, RetailDemoRowToInsert {
    private IncludableUUID loginSessionId = IncludableUUID.notIncluded();
    private IncludableBoolean isSignedOut = IncludableBoolean.notIncluded();
    private IncludableOffsetDateTime createdAt = IncludableOffsetDateTime.notIncluded();
    private IncludableOffsetDateTime lastUpdatedAt = IncludableOffsetDateTime.notIncluded();
    private UserAccountReference userAccount;


    @ConstructorProperties({"userAccount"})
    public LoginSessionToInsert(
            @NotNull UserAccountReference userAccount){
        NullUtils.requireAllNonNull(userAccount);
        this.userAccount = userAccount;
    }
    public @NotNull IncludableUUID getLoginSessionId(){
        return loginSessionId;
    }

    public void setLoginSessionId(@NotNull IncludableUUID loginSessionId){
        Objects.requireNonNull(loginSessionId);
        this.loginSessionId = loginSessionId;
    }

    public void setLoginSessionIdIncludableOf(@NotNull UUID loginSessionId){
        Objects.requireNonNull(loginSessionId);
        this.loginSessionId = IncludableUUID.of(loginSessionId);
    }

    public @NotNull IncludableBoolean getIsSignedOut(){
        return isSignedOut;
    }

    public void setIsSignedOut(@NotNull IncludableBoolean isSignedOut){
        Objects.requireNonNull(isSignedOut);
        this.isSignedOut = isSignedOut;
    }

    public void setIsSignedOutIncludableOf(boolean isSignedOut){
        this.isSignedOut = IncludableBoolean.of(isSignedOut);
    }

    public @NotNull IncludableOffsetDateTime getCreatedAt(){
        return createdAt;
    }

    public void setCreatedAt(@NotNull IncludableOffsetDateTime createdAt){
        Objects.requireNonNull(createdAt);
        this.createdAt = createdAt;
    }

    public void setCreatedAtIncludableOf(@NotNull OffsetDateTime createdAt){
        Objects.requireNonNull(createdAt);
        this.createdAt = IncludableOffsetDateTime.of(createdAt);
    }

    public @NotNull IncludableOffsetDateTime getLastUpdatedAt(){
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(@NotNull IncludableOffsetDateTime lastUpdatedAt){
        Objects.requireNonNull(lastUpdatedAt);
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public void setLastUpdatedAtIncludableOf(@NotNull OffsetDateTime lastUpdatedAt){
        Objects.requireNonNull(lastUpdatedAt);
        this.lastUpdatedAt = IncludableOffsetDateTime.of(lastUpdatedAt);
    }

    public @NotNull UserAccountReference getUserAccount(){
        return userAccount;
    }

    public void setUserAccount(@NotNull UserAccountReference userAccount){
        Objects.requireNonNull(userAccount);
        this.userAccount = userAccount;
    }

}