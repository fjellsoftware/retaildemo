/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.autogenerated.orm;

import java.util.UUID;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public sealed interface MetricKindReference extends RetailDemoRowReference permits MetricKindToInsert, RetailDemoIdReferenceLong {
    static MetricKindReference of(long metricKindId){
        return new RetailDemoIdReferenceLong(metricKindId);
    }
}
