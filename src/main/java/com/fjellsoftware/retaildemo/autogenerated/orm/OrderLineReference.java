/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.autogenerated.orm;

import java.util.UUID;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public sealed interface OrderLineReference extends RetailDemoRowReference permits OrderLineToInsert, RetailDemoIdReferenceInt {
    static OrderLineReference of(int orderLineId){
        return new RetailDemoIdReferenceInt(orderLineId);
    }
}
