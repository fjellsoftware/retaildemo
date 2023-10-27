/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.autogenerated.orm;

import io.loppi.includablevalues.*;
import io.loppi.orm.LoppiAutogeneratedClassesParser;
import io.loppi.orm.metamodel.*;
import com.fjellsoftware.javafunctionalutils.NullUtils;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

import java.beans.ConstructorProperties;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public final class MetricValueResult implements RetailDemoDatabaseQueryResult {
    private final IncludableLong metricValueId;
    private final IncludableAssociation<MetricKindResult> metricKind;
    private final IncludableLong metricKindId;
    private final IncludableDouble value;
    private final IncludableOffsetDateTime createdAt;

    @ConstructorProperties({"metricValueId", "metricKind", "metricKindId", "value", "createdAt"})
    public MetricValueResult(
            IncludableLong metricValueId, IncludableAssociation<MetricKindResult> metricKind, 
            IncludableLong metricKindId, IncludableDouble value, IncludableOffsetDateTime createdAt){
        NullUtils.requireAllNonNull(metricValueId, metricKind, metricKindId, value, createdAt);
        this.metricValueId = metricValueId;
        this.metricKind = metricKind;
        this.metricKindId = metricKindId;
        this.value = value;
        this.createdAt = createdAt;
    }

    public IncludableLong getMetricValueId(){
        return metricValueId;
    }

    public IncludableAssociation<MetricKindResult> getMetricKind(){
        return metricKind;
    }

    public IncludableLong getMetricKindId(){
        return metricKindId;
    }

    public IncludableDouble getValue(){
        return value;
    }

    public IncludableOffsetDateTime getCreatedAt(){
        return createdAt;
    }


}