package com.fjellsoftware.retaildemo.autogenerated.graphql.staff;

import com.fjellsoftware.javafunctionalutils.opt.Opt;
import com.fjellsoftware.javafunctionalutils.ImmutableList;
import com.fjellsoftware.javafunctionalutils.ImmutableMap;
import io.loppi.graphql.document.GraphQLOperationType;
import org.jetbrains.annotations.NotNull;

/////////////////////
/// AUTOGENERATED ///
/////////////////////

public record StaffMutationRequest(
    @NotNull Opt<String> operationName, @NotNull String document, @NotNull ImmutableMap<String,Opt<Object>> variables,
        ImmutableList<StaffMutationRootField> rootFields) implements StaffRequest {
    @Override
    public @NotNull GraphQLOperationType operationType(){
        return GraphQLOperationType.MUTATION;
    }
}