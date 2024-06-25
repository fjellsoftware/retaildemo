package com.fjellsoftware.retaildemo.autogenerated.orm.main;

import io.loppi.orm.includablevalues.*;
import io.loppi.orm.metamodel.*;
import com.fjellsoftware.javafunctionalutils.NullUtils;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

import java.beans.ConstructorProperties;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public final class PurchaseOrderResult implements RetailDemoTableQueryResult {
    private final IncludableInt purchaseOrderId;
    private final IncludableAssociation<CountryResult> country;
    private final IncludableInt countryId;
    private final IncludableOffsetDateTime createdAt;
    private final IncludableOffsetDateTime lastUpdatedAt;
    private final IncludableString orderStatus;
    private final IncludableString address;
    private final IncludableString name;
    private final IncludableString phoneNumber;
    private final IncludableNullableAssociation<UserAccountResult> customer;
    private final IncludableNullableUUID customerId;
    private final IncludableAssociationList<OrderLineResult> orderLines;

    @ConstructorProperties({"purchaseOrderId", "country", "countryId", "createdAt", "lastUpdatedAt", "orderStatus", 
        "address", "name", "phoneNumber", "customer", "customerId", "orderLines"})
    public PurchaseOrderResult(
            @NotNull IncludableInt purchaseOrderId, @NotNull IncludableAssociation<CountryResult> country, 
            @NotNull IncludableInt countryId, @NotNull IncludableOffsetDateTime createdAt, 
            @NotNull IncludableOffsetDateTime lastUpdatedAt, @NotNull IncludableString orderStatus, 
            @NotNull IncludableString address, @NotNull IncludableString name, @NotNull IncludableString phoneNumber, 
            @NotNull IncludableNullableAssociation<UserAccountResult> customer, 
            @NotNull IncludableNullableUUID customerId, @NotNull IncludableAssociationList<OrderLineResult> orderLines){
        NullUtils.requireAllNonNull(purchaseOrderId, country, countryId, createdAt, lastUpdatedAt, orderStatus, 
                address, name, phoneNumber, customer, customerId, orderLines);
        this.purchaseOrderId = purchaseOrderId;
        this.country = country;
        this.countryId = countryId;
        this.createdAt = createdAt;
        this.lastUpdatedAt = lastUpdatedAt;
        this.orderStatus = orderStatus;
        this.address = address;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.customer = customer;
        this.customerId = customerId;
        this.orderLines = orderLines;
    }

    public @NotNull IncludableInt getPurchaseOrderId(){
        return purchaseOrderId;
    }

    public @NotNull IncludableAssociation<CountryResult> getCountry(){
        return country;
    }

    public @NotNull IncludableInt getCountryId(){
        return countryId;
    }

    public @NotNull IncludableOffsetDateTime getCreatedAt(){
        return createdAt;
    }

    public @NotNull IncludableOffsetDateTime getLastUpdatedAt(){
        return lastUpdatedAt;
    }

    public @NotNull IncludableString getOrderStatus(){
        return orderStatus;
    }

    public @NotNull IncludableString getAddress(){
        return address;
    }

    public @NotNull IncludableString getName(){
        return name;
    }

    public @NotNull IncludableString getPhoneNumber(){
        return phoneNumber;
    }

    public @NotNull IncludableNullableAssociation<UserAccountResult> getCustomer(){
        return customer;
    }

    public @NotNull IncludableNullableUUID getCustomerId(){
        return customerId;
    }

    public @NotNull IncludableAssociationList<OrderLineResult> getOrderLines(){
        return orderLines;
    }


}