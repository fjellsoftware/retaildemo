package com.fjellsoftware.retaildemo.autogenerated.graphql.customer;

import com.fjellsoftware.javafunctionalutils.ImmutableList;
import com.fjellsoftware.javafunctionalutils.opt.Opt;
import com.fjellsoftware.javafunctionalutils.opt.Some;
import com.fjellsoftware.retaildemo.autogenerated.orm.RetailDemoRowToMutate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import com.fjellsoftware.javafunctionalutils.NullUtils;
import com.fjellsoftware.retaildemo.autogenerated.orm.PurchaseOrderToInsert;
import com.fjellsoftware.retaildemo.autogenerated.orm.OrderLineToInsert;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public record OrderInsertMutationInput(
        @NotNull PurchaseOrderToInsert purchaseOrderToInsert,
        @NotNull ImmutableList<OrderLineToInsert> orderLinesToInsert) implements Iterable<RetailDemoRowToMutate> {
    public OrderInsertMutationInput {
        NullUtils.requireAllNonNull(purchaseOrderToInsert, orderLinesToInsert);
    }

    @NotNull
    @Override
    public Iterator<RetailDemoRowToMutate> iterator(){
        List<RetailDemoRowToMutate> rowsToMutate = new ArrayList<>();
            rowsToMutate.add(purchaseOrderToInsert);
        for(OrderLineToInsert orderLineToInsert : orderLinesToInsert) {
            rowsToMutate.add(orderLineToInsert);
        }
        return rowsToMutate.iterator();
    }
}