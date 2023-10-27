/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.autogenerated.orm;

import io.loppi.includablevalues.*;
import io.loppi.orm.LoppiAutogeneratedClassesParser;
import io.loppi.orm.metamodel.*;
import com.fjellsoftware.javafunctionalutils.NullUtils;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

import java.beans.ConstructorProperties;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public final class ProductResult implements RetailDemoDatabaseQueryResult {
    private final IncludableInt productId;
    private final IncludableString stockCode;
    private final IncludableString description;
    private final IncludableBigDecimal currentUnitPrice;
    private final IncludableOffsetDateTime createdAt;
    private final IncludableOffsetDateTime lastUpdatedAt;
    private final IncludableAssociationList<OrderLineResult> orderLines;

    @ConstructorProperties({"productId", "stockCode", "description", "currentUnitPrice", "createdAt", "lastUpdatedAt", 
        "orderLines"})
    public ProductResult(
            IncludableInt productId, IncludableString stockCode, IncludableString description, 
            IncludableBigDecimal currentUnitPrice, IncludableOffsetDateTime createdAt, 
            IncludableOffsetDateTime lastUpdatedAt, IncludableAssociationList<OrderLineResult> orderLines){
        NullUtils.requireAllNonNull(productId, stockCode, description, currentUnitPrice, createdAt, lastUpdatedAt,
                orderLines);
        this.productId = productId;
        this.stockCode = stockCode;
        this.description = description;
        this.currentUnitPrice = currentUnitPrice;
        this.createdAt = createdAt;
        this.lastUpdatedAt = lastUpdatedAt;
        this.orderLines = orderLines;
    }

    public IncludableInt getProductId(){
        return productId;
    }

    public IncludableString getStockCode(){
        return stockCode;
    }

    public IncludableString getDescription(){
        return description;
    }

    public IncludableBigDecimal getCurrentUnitPrice(){
        return currentUnitPrice;
    }

    public IncludableOffsetDateTime getCreatedAt(){
        return createdAt;
    }

    public IncludableOffsetDateTime getLastUpdatedAt(){
        return lastUpdatedAt;
    }

    public IncludableAssociationList<OrderLineResult> getOrderLines(){
        return orderLines;
    }


}