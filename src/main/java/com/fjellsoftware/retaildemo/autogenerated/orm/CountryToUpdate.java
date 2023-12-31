/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.autogenerated.orm;

import java.util.UUID;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.Objects;

import io.loppi.includablevalues.*;
import io.loppi.orm.metamodel.*;
import io.loppi.orm.LoppiAutogeneratedClassesParser;
import org.jetbrains.annotations.NotNull;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public final class CountryToUpdate implements RetailDemoRowToUpdate {
    private final int whereCountryId;
    private IncludableString name = IncludableString.notIncluded();

    public CountryToUpdate(int whereCountryId){
        this.whereCountryId = whereCountryId;
    }

    public int getWhereCountryId(){
        return whereCountryId;
    }

    public IncludableString getName(){
        return name;
    }

    public void setName(@NotNull IncludableString name){
        Objects.requireNonNull(name);
        this.name = name;
    }

    public void setNameIncludableOf(@NotNull String name){
        Objects.requireNonNull(name);
        this.name = IncludableString.of(name);
    }

    @Override
    public String toString(){
        return LoppiAutogeneratedClassesParser.tryRowToMutateToString(this);
    }

}
