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

public final class ProductMeta{
    private static final Class<ProductResult> referenceClass = ProductResult.class;

    private static final String PRODUCT_ID_FIELD_NAME = "productId";
    private static final String STOCK_CODE_FIELD_NAME = "stockCode";
    private static final String DESCRIPTION_FIELD_NAME = "description";
    private static final String CURRENT_UNIT_PRICE_FIELD_NAME = "currentUnitPrice";
    private static final String CREATED_AT_FIELD_NAME = "createdAt";
    private static final String LAST_UPDATED_AT_FIELD_NAME = "lastUpdatedAt";
    private static final String ORDER_LINES_FIELD_NAME = "orderLines";

    static final ScalarAttributeReference<ProductResult, IncludableInt> productIdInternalReference = 
        new ScalarAttributeReference<>(referenceClass, PRODUCT_ID_FIELD_NAME, IncludableInt.class);
    static final PluralDerivedAssociationAttributeReference<ProductResult, OrderLineResult> orderLinesInternalReference = 
        new PluralDerivedAssociationAttributeReference<>(referenceClass, ORDER_LINES_FIELD_NAME, OrderLineResult.class);

    public final ScalarAttribute<ProductResult, IncludableInt> productId;
    public final ScalarAttribute<ProductResult, IncludableString> stockCode;
    public final ScalarAttribute<ProductResult, IncludableString> description;
    public final ScalarAttribute<ProductResult, IncludableBigDecimal> currentUnitPrice;
    public final ScalarAttribute<ProductResult, IncludableOffsetDateTime> createdAt;
    public final ScalarAttribute<ProductResult, IncludableOffsetDateTime> lastUpdatedAt;
    public final PluralDerivedAssociationAttribute<ProductResult, OrderLineResult> orderLines;

    private final EntityMetadata<ProductResult> entityMetadata;

    @SuppressWarnings({"unchecked"})
    public ProductMeta(LoppiMetamodel<RetailDemoDatabaseQueryResult> loppiMetamodel){
        this.entityMetadata = loppiMetamodel.getEntityMetadata(referenceClass).getOrThrow();
        this.productId = (ScalarAttribute<ProductResult, IncludableInt>) entityMetadata.tryGetScalarAttribute(PRODUCT_ID_FIELD_NAME).getOrThrow();
        this.stockCode = (ScalarAttribute<ProductResult, IncludableString>) entityMetadata.tryGetScalarAttribute(STOCK_CODE_FIELD_NAME).getOrThrow();
        this.description = (ScalarAttribute<ProductResult, IncludableString>) entityMetadata.tryGetScalarAttribute(DESCRIPTION_FIELD_NAME).getOrThrow();
        this.currentUnitPrice = (ScalarAttribute<ProductResult, IncludableBigDecimal>) entityMetadata.tryGetScalarAttribute(CURRENT_UNIT_PRICE_FIELD_NAME).getOrThrow();
        this.createdAt = (ScalarAttribute<ProductResult, IncludableOffsetDateTime>) entityMetadata.tryGetScalarAttribute(CREATED_AT_FIELD_NAME).getOrThrow();
        this.lastUpdatedAt = (ScalarAttribute<ProductResult, IncludableOffsetDateTime>) entityMetadata.tryGetScalarAttribute(LAST_UPDATED_AT_FIELD_NAME).getOrThrow();
        this.orderLines = (PluralDerivedAssociationAttribute<ProductResult, OrderLineResult>) entityMetadata.tryGetAssociationAttribute(ORDER_LINES_FIELD_NAME).getOrThrow();
    }

    public EntityMetadata<ProductResult> getEntityMetadata(){
        return entityMetadata;
    }
}
