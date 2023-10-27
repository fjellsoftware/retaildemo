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

public final class MetricKindResult implements RetailDemoDatabaseQueryResult {
    private final IncludableLong metricKindId;
    private final IncludableString name;
    private final IncludableOffsetDateTime createdAt;
    private final IncludableAssociationList<MetricValueResult> metricValues;

    @ConstructorProperties({"metricKindId", "name", "createdAt", "metricValues"})
    public MetricKindResult(
            IncludableLong metricKindId, IncludableString name, IncludableOffsetDateTime createdAt, 
            IncludableAssociationList<MetricValueResult> metricValues){
        NullUtils.requireAllNonNull(metricKindId, name, createdAt, metricValues);
        this.metricKindId = metricKindId;
        this.name = name;
        this.createdAt = createdAt;
        this.metricValues = metricValues;
    }

    public IncludableLong getMetricKindId(){
        return metricKindId;
    }

    public IncludableString getName(){
        return name;
    }

    public IncludableOffsetDateTime getCreatedAt(){
        return createdAt;
    }

    public IncludableAssociationList<MetricValueResult> getMetricValues(){
        return metricValues;
    }


}
