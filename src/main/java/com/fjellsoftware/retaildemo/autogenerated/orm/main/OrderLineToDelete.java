package com.fjellsoftware.retaildemo.autogenerated.orm.main;

import io.loppi.orm.metamodel.RowToDelete;
import org.jetbrains.annotations.NotNull;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public final class OrderLineToDelete implements RetailDemoRowToDelete, RowToDelete {
    private final int whereOrderLineId;

    public OrderLineToDelete(int whereOrderLineId){
        this.whereOrderLineId = whereOrderLineId;
    }

    public int getWhereOrderLineId(){
        return whereOrderLineId;
    }

}