/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.autogenerated.orm;

import java.util.UUID;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.Objects;

import io.loppi.includablevalues.*;
import io.loppi.orm.metamodel.*;
import io.loppi.orm.LoppiAutogeneratedClassesParser;
import org.jetbrains.annotations.NotNull;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public final class OrderLineToUpdate implements RetailDemoRowToUpdate {
    private IncludableRowReference<PurchaseOrderReference> purchaseOrder = IncludableRowReference.notIncluded();
    private IncludableInt quantity = IncludableInt.notIncluded();
    private IncludableBigDecimal unitPrice = IncludableBigDecimal.notIncluded();
    private IncludableRowReference<ProductReference> product = IncludableRowReference.notIncluded();
    private final int whereOrderLineId;
    private IncludableOffsetDateTime createdAt = IncludableOffsetDateTime.notIncluded();

    public OrderLineToUpdate(int whereOrderLineId){
        this.whereOrderLineId = whereOrderLineId;
    }

    public IncludableRowReference<PurchaseOrderReference> getPurchaseOrder(){
        return purchaseOrder;
    }

    public void setPurchaseOrder(@NotNull IncludableRowReference<PurchaseOrderReference> purchaseOrder){
        Objects.requireNonNull(purchaseOrder);
        this.purchaseOrder = purchaseOrder;
    }

    public void setPurchaseOrderIncludableOf(@NotNull PurchaseOrderReference purchaseOrder){
        Objects.requireNonNull(purchaseOrder);
        this.purchaseOrder = IncludableRowReference.of(purchaseOrder);
    }

    public IncludableInt getQuantity(){
        return quantity;
    }

    public void setQuantity(@NotNull IncludableInt quantity){
        Objects.requireNonNull(quantity);
        this.quantity = quantity;
    }

    public void setQuantityIncludableOf(int quantity){
        this.quantity = IncludableInt.of(quantity);
    }

    public IncludableBigDecimal getUnitPrice(){
        return unitPrice;
    }

    public void setUnitPrice(@NotNull IncludableBigDecimal unitPrice){
        Objects.requireNonNull(unitPrice);
        this.unitPrice = unitPrice;
    }

    public void setUnitPriceIncludableOf(@NotNull BigDecimal unitPrice){
        Objects.requireNonNull(unitPrice);
        this.unitPrice = IncludableBigDecimal.of(unitPrice);
    }

    public IncludableRowReference<ProductReference> getProduct(){
        return product;
    }

    public void setProduct(@NotNull IncludableRowReference<ProductReference> product){
        Objects.requireNonNull(product);
        this.product = product;
    }

    public void setProductIncludableOf(@NotNull ProductReference product){
        Objects.requireNonNull(product);
        this.product = IncludableRowReference.of(product);
    }

    public int getWhereOrderLineId(){
        return whereOrderLineId;
    }

    public IncludableOffsetDateTime getCreatedAt(){
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

    @Override
    public String toString(){
        return LoppiAutogeneratedClassesParser.tryRowToMutateToString(this);
    }

}
