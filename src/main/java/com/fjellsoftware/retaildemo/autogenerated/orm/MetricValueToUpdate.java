package com.fjellsoftware.retaildemo.autogenerated.orm;

import java.util.UUID;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.Objects;

import io.loppi.orm.includablevalues.*;
import io.loppi.orm.metamodel.*;
import org.jetbrains.annotations.NotNull;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public final class MetricValueToUpdate implements RetailDemoRowToUpdate {
    private final long whereMetricValueId;
    private IncludableRowReference<MetricKindReference> metricKind = IncludableRowReference.notIncluded();
    private IncludableDouble value = IncludableDouble.notIncluded();
    private IncludableOffsetDateTime createdAt = IncludableOffsetDateTime.notIncluded();

    public MetricValueToUpdate(long whereMetricValueId){
        this.whereMetricValueId = whereMetricValueId;
    }

    public long getWhereMetricValueId(){
        return whereMetricValueId;
    }

    public @NotNull IncludableRowReference<MetricKindReference> getMetricKind(){
        return metricKind;
    }

    public void setMetricKind(@NotNull IncludableRowReference<MetricKindReference> metricKind){
        Objects.requireNonNull(metricKind);
        this.metricKind = metricKind;
    }

    public void setMetricKindIncludableOf(@NotNull MetricKindReference metricKind){
        Objects.requireNonNull(metricKind);
        this.metricKind = IncludableRowReference.of(metricKind);
    }

    public @NotNull IncludableDouble getValue(){
        return value;
    }

    public void setValue(@NotNull IncludableDouble value){
        Objects.requireNonNull(value);
        this.value = value;
    }

    public void setValueIncludableOf(double value){
        this.value = IncludableDouble.of(value);
    }

    public @NotNull IncludableOffsetDateTime getCreatedAt(){
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

}