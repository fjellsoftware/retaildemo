package com.fjellsoftware.retaildemo.autogenerated.graphql.customer;

import org.jetbrains.annotations.NotNull;
import com.fjellsoftware.javafunctionalutils.NullUtils;
import io.loppi.graphql.document.GraphQLOutputField;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public record DemoDowngradeToCustomerMutationField(
        @NotNull GraphQLOutputField rawField,
        @NotNull DemoDowngradeToCustomerMutationInput input)
        implements CustomerOrdinaryMutationRootField<IgnoredResult> {
    public DemoDowngradeToCustomerMutationField {
        NullUtils.requireAllNonNull(rawField, input);
    }
}