package com.fjellsoftware.retaildemo.autogenerated.orm.main;

import java.util.UUID;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public sealed interface CountryReference extends RetailDemoRowReference permits CountryToInsert, RetailDemoIdReferenceInt {
    static CountryReference of(int countryId){
        return new RetailDemoIdReferenceInt(countryId);
    }
}