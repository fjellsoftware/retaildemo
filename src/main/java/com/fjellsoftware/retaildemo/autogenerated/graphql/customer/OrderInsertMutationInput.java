/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.autogenerated.graphql.customer;

import com.fjellsoftware.javafunctionalutils.ImmutableList;
import com.fjellsoftware.javafunctionalutils.opt.Opt;
import com.fjellsoftware.javafunctionalutils.opt.Some;
import io.loppi.orm.metamodel.RowToMutate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import com.fjellsoftware.retaildemo.autogenerated.orm.PurchaseOrderToInsert;
import com.fjellsoftware.retaildemo.autogenerated.orm.OrderLineToInsert;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public record OrderInsertMutationInput(
        PurchaseOrderToInsert purchaseOrderToInsert,
        ImmutableList<OrderLineToInsert> orderLines) implements Iterable<RowToMutate> {
    @NotNull
    @Override
    public Iterator<RowToMutate> iterator(){
        List<RowToMutate> rowsToMutate = new ArrayList<>();
            rowsToMutate.add(purchaseOrderToInsert);
        for(OrderLineToInsert orderLineToInsert : orderLines) {
        rowsToMutate.add(orderLineToInsert);
        }
        return rowsToMutate.iterator();
    }
}
