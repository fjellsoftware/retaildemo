/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.autogenerated.orm;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

import java.beans.ConstructorProperties;import io.loppi.includablevalues.*;
import io.loppi.orm.LoppiAutogeneratedClassesParser;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public final class MetricKindToInsert implements MetricKindReference, RetailDemoRowToInsert {
    private String name;
    private IncludableOffsetDateTime createdAt = IncludableOffsetDateTime.notIncluded();


    @ConstructorProperties({"name"})
    public MetricKindToInsert(
            @NotNull String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }

    public void setName(@NotNull String name){
        Objects.requireNonNull(name);
        this.name = name;
    }

    public IncludableOffsetDateTime getCreatedAt(){
        return createdAt;
    }

    public void setCreatedAt(@NotNull IncludableOffsetDateTime createdAt){
        Objects.requireNonNull(createdAt);
        this.createdAt = createdAt;
    }

    public void setCreatedAtIncludableOf(@NotNull OffsetDateTime createdAt){
        Objects.requireNonNull(createdAt);
        this.createdAt = IncludableOffsetDateTime.of(createdAt);
    }

    @Override
    public String toString(){
        return LoppiAutogeneratedClassesParser.tryRowToMutateToString(this);
    }

}
