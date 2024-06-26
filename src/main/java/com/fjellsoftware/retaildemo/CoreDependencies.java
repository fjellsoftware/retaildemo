/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

import com.fjellsoftware.retaildemo.authorizationcommon.HCaptchaVerifier;
import com.fjellsoftware.retaildemo.autogenerated.orm.main.RetailDemoORMService;
import com.fjellsoftware.retaildemo.autogenerated.orm.main.RetailDemoMeta;
import com.fjellsoftware.retaildemo.autogenerated.orm.metrics.MetricsORMService;
import com.fjellsoftware.retaildemo.demo.DemoDomain;
import com.fjellsoftware.retaildemo.domain.*;
import com.fjellsoftware.retaildemo.util.FileUtils;
import com.zaxxer.hikari.HikariDataSource;
import io.loppi.graphql.GraphQLService;
import io.loppi.graphql.integration.orm.GraphQLORMSchemaConfiguration;
import io.loppi.graphql.integration.orm.serviceconfiguration.GraphQLORMServiceConfigurationVersionBeta;
import io.loppi.graphql.integration.orm.table.GraphQLTableConfiguration;
import io.loppi.graphql.outputobjecttypes.UUIDTokenResultType;
import io.loppi.graphql.schema.GraphQLInputFieldDefinition;
import io.loppi.graphql.schema.GraphQLOutputFieldDefinition;
import io.loppi.graphql.schema.GraphQLOutputObjectType;
import io.loppi.graphql.schema.GraphQLScalarTypeCategory;
import io.loppi.orm.serviceconfiguration.ORMServiceConfigurationVersionBeta;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class CoreDependencies {
    private final HikariDataSource dataSource;
    private final RetailDemoORMService loppiService;
    private final MetricsORMService metricsService;
    private final GraphQLService customerGraphQLServiceRaw;
    private final GraphQLService staffGraphQLServiceRaw;

    public static final MessageDigest sha256Digest = createSHA256MessageDigest();

    public CoreDependencies(ApplicationConfiguration configuration) {
        this.dataSource = createMainDataSource(configuration.getCredentialsDirectory());
        this.loppiService = new RetailDemoORMService(dataSource, new ORMServiceConfigurationVersionBeta());
        HikariDataSource metricsDataSource = createDataSource(configuration.getCredentialsDirectory(), "metrics", 5);
        this.metricsService = new MetricsORMService(metricsDataSource, new ORMServiceConfigurationVersionBeta());
        this.customerGraphQLServiceRaw = createCustomerGraphQLService(loppiService);
        this.staffGraphQLServiceRaw = createStaffGraphQLService(loppiService);
    }

    private static MessageDigest createSHA256MessageDigest(){
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new ApplicationInternalException("Failed to initialize SHA-256 message digest.", e);
        }
    }

    private static final String database_secret_fileName = "database_secret";
    public static HikariDataSource createMainDataSource(String credentialsDirectory) {
        return createDataSource(credentialsDirectory, "retail_demo", 20);
    }

    public static HikariDataSource createDataSource(String credentialsDirectory, String databaseName, int poolSize) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/"+databaseName);
        dataSource.setUsername("application");
        String databaseSecret = FileUtils.loadFirstLineFromFile(credentialsDirectory, database_secret_fileName);
        dataSource.setPassword(databaseSecret);
        dataSource.setConnectionTimeout(1000);
        dataSource.setMaximumPoolSize(poolSize);
        return dataSource;
    }

    private GraphQLService createCustomerGraphQLService(RetailDemoORMService loppiService){
        GraphQLORMSchemaConfiguration schemaConfiguration = createCustomerGraphQLSchemaConfiguration(loppiService.getMeta());
        return loppiService.createORMIntegratedGraphQLService(schemaConfiguration, new GraphQLORMServiceConfigurationVersionBeta());
    }

    private GraphQLService createStaffGraphQLService(RetailDemoORMService loppiService){
        return loppiService.createORMIntegratedGraphQLService(createStaffGraphQLSchemaConfiguration(loppiService.getMeta()),
                new GraphQLORMServiceConfigurationVersionBeta());
    }

    private GraphQLORMSchemaConfiguration createCustomerGraphQLSchemaConfiguration(RetailDemoMeta retailDemoMeta){
        List<GraphQLTableConfiguration<?>> tableConfigurations = List.of(
                ProductDomain.createCustomerApiTableConfiguration(retailDemoMeta.getProductMeta()),
                OrderDomain.createCustomerApiTableConfiguration(retailDemoMeta.getPurchaseOrderMeta()),
                OrderLineDomain.createCustomerApiTableConfiguration(retailDemoMeta.getOrderLineMeta()),
                UserDomain.createCustomerApiTableConfiguration(retailDemoMeta.getUserAccountMeta())
        );
        List<GraphQLOutputFieldDefinition> ordinaryMutations = List.of(
                LoginSessionDomain.createLoginOrdinaryMutationDefinition(),
                LoginSessionDomain.createSignOutOrdinaryMutationDefinition(),
                UserDomain.createUpdatePasswordOrdinaryMutationDefinition(),
                UserDomain.createSignUpOrdinaryMutationDefiniton(),
                DemoDomain.upgradeToStaffOrdinaryMutationDefinition(),
                DemoDomain.downGradeToCustomerOrdinaryMutationDefiniton(),
                createVerifyCaptchaMutationDefinition(),
                createGetBcryptSaltMutationDefinition());
        return GraphQLORMSchemaConfiguration.fromTableConfigurations(tableConfigurations)
                .setOrdinaryGraphQLMutationFields(ordinaryMutations);
    }

    private GraphQLOutputFieldDefinition createVerifyCaptchaMutationDefinition(){
        GraphQLInputFieldDefinition tokenArgument = GraphQLInputFieldDefinition.builder().setName("token")
                .setType(GraphQLScalarTypeCategory.String).setNotNull(true).build();
        return GraphQLOutputFieldDefinition.builder().setName("verifyCaptcha")
                .setDescription(String.format("Checks with a third party if the submitted token is a valid captcha response. " +
                        "If successful sets cookie \"%s\" and returns the token. Does no mutations", HCaptchaVerifier.H_CAPTCHA_TOKEN_COOKIE_NAME))
                .setType(UUIDTokenResultType.get())
                .setNotNull(true)
                .setArguments(List.of(tokenArgument))
                .build();
    }

    public static final String GET_BCRYPT_SALT_FIELD_NAME = "getBcryptSalt";
    private GraphQLOutputFieldDefinition createGetBcryptSaltMutationDefinition(){
        GraphQLOutputFieldDefinition saltField = GraphQLOutputFieldDefinition.builder().setName("salt")
                .setNotNull(true).setType(GraphQLScalarTypeCategory.String).build();
        GraphQLOutputObjectType saltResult = GraphQLOutputObjectType.builder().setName("SaltResult")
                .addField(saltField).build();
        GraphQLInputFieldDefinition usernameArgument = GraphQLInputFieldDefinition.builder().setName("username")
                .setType(GraphQLScalarTypeCategory.String).setNotNull(true).build();
        return GraphQLOutputFieldDefinition.builder().setName(GET_BCRYPT_SALT_FIELD_NAME)
                .setDescription("Returns the salt that should be used with bcrypt when hashing the password " +
                        "client-side when signing up or logging in. Does no mutations.")
                .setType(saltResult)
                .setNotNull(true)
                .addArgument(usernameArgument)
                .build();
    }

    private GraphQLORMSchemaConfiguration createStaffGraphQLSchemaConfiguration(RetailDemoMeta retailDemoMeta){
        List<GraphQLTableConfiguration<?>> tableConfigurations = List.of(
                ProductDomain.createStaffApiTableConfiguration(retailDemoMeta.getProductMeta()),
                OrderDomain.createStaffApiTableConfiguration(retailDemoMeta.getPurchaseOrderMeta()),
                OrderLineDomain.createStaffApiTableConfiguration(retailDemoMeta.getOrderLineMeta()),
                UserDomain.createStaffApiTableConfiguration(retailDemoMeta.getUserAccountMeta()),
                LoginSessionDomain.createStaffApiTableConfiguration(retailDemoMeta.getLoginSessionMeta())
        );
        return GraphQLORMSchemaConfiguration.fromTableConfigurations(tableConfigurations);
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public RetailDemoORMService getLoppiService() {
        return loppiService;
    }

    public MetricsORMService getMetricsService(){
        return metricsService;
    }

    public GraphQLService getCustomerGraphQLServiceRaw() {
        return customerGraphQLServiceRaw;
    }

    public GraphQLService getStaffGraphQLServiceRaw() {
        return staffGraphQLServiceRaw;
    }


}
