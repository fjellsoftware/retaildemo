package com.fjellsoftware.retaildemo.autogenerated.graphql.staff;

import com.fjellsoftware.javafunctionalutils.ImmutableList;
import io.loppi.graphql.integration.orm.table.mutation.GraphQLTableMutationField;
import com.fjellsoftware.retaildemo.autogenerated.orm.main.RetailDemoRowToMutate;
import io.loppi.graphql.integration.orm.table.query.GraphQLTableQueryFieldNoArguments;
import com.fjellsoftware.retaildemo.autogenerated.orm.main.PurchaseOrderResult;
import com.fjellsoftware.retaildemo.autogenerated.orm.main.PurchaseOrderToUpdate;
import org.jetbrains.annotations.NotNull;
import com.fjellsoftware.javafunctionalutils.NullUtils;
import java.util.ArrayList;
import java.util.List;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public record OrderStatusBatchUpdateMutationField(
        @NotNull GraphQLTableQueryFieldNoArguments<PurchaseOrderResult> queryField,
        @NotNull ImmutableList<PurchaseOrderToUpdate> purchaseOrdersToUpdate
) implements StaffTableMutationRootField, GraphQLTableMutationField<PurchaseOrderResult> {

    public OrderStatusBatchUpdateMutationField {
        NullUtils.requireAllNonNull(purchaseOrdersToUpdate, queryField);
    }


    @Override
    public @NotNull Iterable<RetailDemoRowToMutate> allRowsToMutate() {
        List<RetailDemoRowToMutate> rowsToMutate = new ArrayList<>();
        for (var rowToMutate : purchaseOrdersToUpdate) {
            rowsToMutate.add(rowToMutate);
        }
        return rowsToMutate;
    }
}