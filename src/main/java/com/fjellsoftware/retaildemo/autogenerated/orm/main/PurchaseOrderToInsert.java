package com.fjellsoftware.retaildemo.autogenerated.orm.main;

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

public final class PurchaseOrderToInsert implements PurchaseOrderReference, RetailDemoRowToInsert {
    private CountryReference country;
    private IncludableOffsetDateTime createdAt = IncludableOffsetDateTime.notIncluded();
    private IncludableOffsetDateTime lastUpdatedAt = IncludableOffsetDateTime.notIncluded();
    private IncludableString orderStatus = IncludableString.notIncluded();
    private String address;
    private String name;
    private String phoneNumber;
    private IncludableNullableRowReference<UserAccountReference> customer = IncludableNullableRowReference.notIncluded();


    @ConstructorProperties({"country", "address", "name", "phoneNumber"})
    public PurchaseOrderToInsert(
            @NotNull CountryReference country, @NotNull String address, @NotNull String name, 
            @NotNull String phoneNumber){
        NullUtils.requireAllNonNull(country, address, phoneNumber, name);
        this.country = country;
        this.address = address;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }
    public @NotNull CountryReference getCountry(){
        return country;
    }

    public void setCountry(@NotNull CountryReference country){
        Objects.requireNonNull(country);
        this.country = country;
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

    public @NotNull IncludableString getOrderStatus(){
        return orderStatus;
    }

    public void setOrderStatus(@NotNull IncludableString orderStatus){
        Objects.requireNonNull(orderStatus);
        this.orderStatus = orderStatus;
    }

    public void setOrderStatusIncludableOf(@NotNull String orderStatus){
        Objects.requireNonNull(orderStatus);
        this.orderStatus = IncludableString.of(orderStatus);
    }

    public @NotNull String getAddress(){
        return address;
    }

    public void setAddress(@NotNull String address){
        Objects.requireNonNull(address);
        this.address = address;
    }

    public @NotNull String getName(){
        return name;
    }

    public void setName(@NotNull String name){
        Objects.requireNonNull(name);
        this.name = name;
    }

    public @NotNull String getPhoneNumber(){
        return phoneNumber;
    }

    public void setPhoneNumber(@NotNull String phoneNumber){
        Objects.requireNonNull(phoneNumber);
        this.phoneNumber = phoneNumber;
    }

    public @NotNull IncludableNullableRowReference<UserAccountReference> getCustomer(){
        return customer;
    }

    public void setCustomer(@NotNull IncludableNullableRowReference<UserAccountReference> customer){
        Objects.requireNonNull(customer);
        this.customer = customer;
    }

    public void setCustomerIncludableOf(@NotNull UserAccountReference customer){
        Objects.requireNonNull(customer);
        this.customer = IncludableNullableRowReference.of(customer);
    }

}