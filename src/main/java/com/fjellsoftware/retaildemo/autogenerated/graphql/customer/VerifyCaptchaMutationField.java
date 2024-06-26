package com.fjellsoftware.retaildemo.autogenerated.graphql.customer;

import org.jetbrains.annotations.NotNull;
import com.fjellsoftware.javafunctionalutils.NullUtils;
import io.loppi.graphql.document.GraphQLOutputField;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public record VerifyCaptchaMutationField(
        @NotNull GraphQLOutputField rawField,
        @NotNull VerifyCaptchaMutationInput input)
        implements CustomerOrdinaryMutationRootField<UUIDTokenResult> {
    public VerifyCaptchaMutationField {
        NullUtils.requireAllNonNull(rawField, input);
    }
}