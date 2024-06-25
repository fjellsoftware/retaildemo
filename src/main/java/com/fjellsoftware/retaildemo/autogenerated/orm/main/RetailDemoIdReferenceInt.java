package com.fjellsoftware.retaildemo.autogenerated.orm.main;

import java.util.UUID;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public final class RetailDemoIdReferenceInt implements
CountryReference, OrderLineReference, ProductReference, PurchaseOrderReference 
    {

    private final int id;

    public RetailDemoIdReferenceInt(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString(){
        return "IdReferenceInt: { id: " + id + "}";
    }

    @Override
    public int hashCode(){
        return id;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(!(obj instanceof RetailDemoIdReferenceInt)){
            return false;
        }
        return ((RetailDemoIdReferenceInt) obj).id == id;
    }


}