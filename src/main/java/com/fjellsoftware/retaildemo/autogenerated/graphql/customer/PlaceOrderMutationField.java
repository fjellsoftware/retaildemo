package com.fjellsoftware.retaildemo.autogenerated.graphql.customer;

import com.fjellsoftware.javafunctionalutils.ImmutableList;
import io.loppi.graphql.integration.orm.table.mutation.GraphQLTableMutationField;
import com.fjellsoftware.retaildemo.autogenerated.orm.RetailDemoRowToMutate;
import io.loppi.graphql.integration.orm.table.query.GraphQLTableQueryFieldNoArguments;
import com.fjellsoftware.retaildemo.autogenerated.orm.PurchaseOrderResult;

import org.jetbrains.annotations.NotNull;
import com.fjellsoftware.javafunctionalutils.NullUtils;
import java.util.List;
import com.fjellsoftware.retaildemo.autogenerated.graphql.customer.*;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public record PlaceOrderMutationField(
        @NotNull GraphQLTableQueryFieldNoArguments<PurchaseOrderResult> queryField,
        @NotNull OrderInsertMutationInput input)
        implements CustomerTableMutationRootField, GraphQLTableMutationField<PurchaseOrderResult>
{

    public PlaceOrderMutationField {
        NullUtils.requireAllNonNull(queryField, input);
    }

    @Override
    public @NotNull Iterable<RetailDemoRowToMutate> allRowsToMutate() {
        return input;
    }
}