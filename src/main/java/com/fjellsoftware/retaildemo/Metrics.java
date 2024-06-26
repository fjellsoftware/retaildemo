/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

import com.fjellsoftware.javafunctionalutils.ImmutableList;
import com.fjellsoftware.javafunctionalutils.ImmutableMap;
import com.fjellsoftware.javafunctionalutils.opt.Opt;
import com.fjellsoftware.javafunctionalutils.opt.Some;
import com.fjellsoftware.retaildemo.autogenerated.orm.metrics.*;
import io.loppi.orm.MutationQueryExecutionResult;
import io.loppi.orm.PostgresExecutionException;
import io.loppi.orm.includablevalues.LongValue;
import io.loppi.orm.includablevalues.StringValue;
import io.loppi.orm.query.QueryExecutionPluralResult;
import io.loppi.orm.query.TableQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Metrics {
    private final Logger logger = LoggerFactory.getLogger(Metrics.class);
    private final MetricsORMService metricsService;
    private final MetricsDatabaseService databaseService;
    private final ImmutableMap<String, Long> initialMetricKindName_ids;

    private static final AtomicLong httpTotalIncoming = new AtomicLong(0);
    private final long httpTotalIncomingId;
    private static final AtomicLong httpTotalOutgoing = new AtomicLong(0);
    private final long httpTotalOutgoingId;

    private static final AtomicLong http200 = new AtomicLong(0);
    private final long http200Id;
    private static final AtomicLong http400 = new AtomicLong(0);
    private final long http400Id;
    private static final AtomicLong http500 = new AtomicLong(0);
    private final long http500Id;

    private static final AtomicLong ipRateLimitTriggers = new AtomicLong(0);
    private final long ipRateLimitTriggersId;
    private static final AtomicLong loginRateLimitTriggers = new AtomicLong(0);
    private final long loginRateLimitTriggersId;
    private static final AtomicLong loginSuccess = new AtomicLong(0);
    private final long loginSuccessId;
    private static final AtomicLong loginError = new AtomicLong(0);
    private final long loginErrorId;

    private static final AtomicLong customerQueriesSuccess = new AtomicLong(0);
    private final long customerQueriesSuccessId;
    private static final AtomicLong customerMutationsSuccess = new AtomicLong(0);
    private final long customerMutationsSuccessId;
    private static final AtomicLong purchaseOrdersSuccess = new AtomicLong(0);
    private final long purchaseOrdersSuccessId;
    private static final AtomicLong staffQueriesSuccess = new AtomicLong(0);
    private final long staffQueriesSuccessId;
    private static final AtomicLong staffMutationsSuccess = new AtomicLong(0);
    private final long staffMutationsSuccessId;

    private static final AtomicLong customerQueriesError = new AtomicLong(0);
    private final long customerQueriesErrorId;
    private static final AtomicLong customerMutationsError = new AtomicLong(0);
    private final long customerMutationsErrorId;
    private static final AtomicLong staffQueriesError = new AtomicLong(0);
    private final long staffQueriesErrorId;
    private static final AtomicLong staffMutationsError = new AtomicLong(0);
    private final long staffMutationsErrorId;

    public Metrics(MetricsORMService metricsService) {
        this.metricsService = metricsService;
        MetricsMeta meta = metricsService.getMeta();

        MetricKindMeta metricKindMeta = meta.getMetricKindMeta();
        this.databaseService = metricsService.getDatabaseServiceJDBC();
        int maximumNumberOfMetrics = 1000;
        TableQuery<MetricKindResult> query =
                databaseService.createTableQueryBuilder(metricKindMeta.getEntityMetadata(), maximumNumberOfMetrics)
                        .includeScalar(metricKindMeta.metricKindId)
                        .includeScalar(metricKindMeta.name)
                        .build();
        QueryExecutionPluralResult<MetricKindResult> queryResult;
        try {
            queryResult = databaseService.executeTableQuery(query);
        } catch (PostgresExecutionException e) {
            throw new ApplicationInternalException("Failed to fetch metric kinds.", e);
        }
        ImmutableList<MetricKindResult> deserialize = queryResult.deserialize();
        if(deserialize.size() == maximumNumberOfMetrics){
            throw new ApplicationInternalException("There may be more than 1000 kinds of metrics in the database, but" +
                    " Loppi server was not built to handle that.");
        }
        Map<String, Long> metricsKindName_Id = new HashMap<>();
        for (MetricKindResult metricKindResult : deserialize) {
            String kindName = ((StringValue) metricKindResult.getName()).value();
            long kindId = ((LongValue) metricKindResult.getMetricKindId()).value();
            metricsKindName_Id.put(kindName, kindId);
        }
        this.initialMetricKindName_ids = new ImmutableMap<>(metricsKindName_Id);

        httpTotalIncomingId = findOrAddMetricKind("http_total_incoming");
        httpTotalOutgoingId = findOrAddMetricKind("http_total_outgoing");

        http200Id = findOrAddMetricKind("http_200");
        http400Id = findOrAddMetricKind("http_400");
        http500Id = findOrAddMetricKind("http_500");

        ipRateLimitTriggersId = findOrAddMetricKind("ip_rate_limit_triggers");
        loginRateLimitTriggersId = findOrAddMetricKind("login_rate_limit_triggers");
        loginSuccessId = findOrAddMetricKind("login_success");
        loginErrorId = findOrAddMetricKind("login_error");

        customerQueriesSuccessId = findOrAddMetricKind("customer_queries_success");
        customerMutationsSuccessId = findOrAddMetricKind("customer_mutations_success");
        purchaseOrdersSuccessId = findOrAddMetricKind("purchase_orders_success");
        staffQueriesSuccessId = findOrAddMetricKind("staff_queries_success");
        staffMutationsSuccessId = findOrAddMetricKind("staff_mutations_success");

        customerQueriesErrorId = findOrAddMetricKind("customer_queries_error");
        customerMutationsErrorId = findOrAddMetricKind("customer_mutations_error");
        staffQueriesErrorId = findOrAddMetricKind("staff_queries_error");
        staffMutationsErrorId = findOrAddMetricKind("staff_mutations_error");
    }

    private static final String kindNamePrefix = "retaildemo_";
    private long findOrAddMetricKind(String kindBaseName){
        String kindName = kindNamePrefix + kindBaseName;
        Opt<Long> kindIdOpt = initialMetricKindName_ids.get(kindName);
        if(kindIdOpt instanceof Some<Long> some){
            return some.value();
        }
        MetricKindMeta metricKindMeta = metricsService.getMeta().getMetricKindMeta();
        TableQuery<MetricKindResult> query =
                databaseService.createTableQueryBuilder(metricKindMeta.getEntityMetadata(), 1)
                        .includeScalar(metricKindMeta.metricKindId)
                        .where().eq(metricKindMeta.name, kindName).end().build();
        MutationQueryExecutionResult<MetricKindResult> executionResult;
        try {
            executionResult = databaseService.executeMutationQuery(List.of(new MetricKindToInsert(kindName)), query);
        } catch (PostgresExecutionException e) {
            throw new ApplicationInternalException(String.format("Failed to create new metric kind: [%s]", kindName), e);
        }
        return ((LongValue) executionResult.getQueryExecutionResult().deserialize().iterator().next().getMetricKindId()).value();
    }


    public void tryGatherAndStoreMetrics(){
        List<MetricsRowToMutate> metricRows = new ArrayList<>();

        addAndResetMetricValue(metricRows, httpTotalIncomingId, httpTotalIncoming);
        addAndResetMetricValue(metricRows, httpTotalOutgoingId, httpTotalOutgoing);

        addAndResetMetricValue(metricRows, http200Id, http200);
        addAndResetMetricValue(metricRows, http400Id, http400);
        addAndResetMetricValue(metricRows, http500Id, http500);

        addAndResetMetricValue(metricRows, ipRateLimitTriggersId, ipRateLimitTriggers);
        addAndResetMetricValue(metricRows, loginRateLimitTriggersId, loginRateLimitTriggers);
        addAndResetMetricValue(metricRows, loginSuccessId, loginSuccess);
        addAndResetMetricValue(metricRows, loginErrorId, loginError);

        addAndResetMetricValue(metricRows, customerQueriesSuccessId, customerQueriesSuccess);
        addAndResetMetricValue(metricRows, customerMutationsSuccessId, customerMutationsSuccess);
        addAndResetMetricValue(metricRows, purchaseOrdersSuccessId, purchaseOrdersSuccess);
        addAndResetMetricValue(metricRows, staffQueriesSuccessId, staffQueriesSuccess);
        addAndResetMetricValue(metricRows, staffMutationsSuccessId, staffMutationsSuccess);

        addAndResetMetricValue(metricRows, customerQueriesErrorId, customerQueriesError);
        addAndResetMetricValue(metricRows, customerMutationsErrorId, customerMutationsError);
        addAndResetMetricValue(metricRows, staffQueriesErrorId, staffQueriesError);
        addAndResetMetricValue(metricRows, staffMutationsErrorId, staffMutationsError);
        try {
            databaseService.executeMutation(metricRows);
        } catch (PostgresExecutionException e) {
            logger.error("Failed to submit metrics to database.", e);
        }
    }

    private void addAndResetMetricValue(List<MetricsRowToMutate> rowsToMutate, long metricKindId, AtomicLong value){
        rowsToMutate.add(new MetricValueToInsert(new MetricsIdReferenceLong(metricKindId), value.getAndSet(0)));
    }

    public static void incrementHttpRequestIncoming(){
        httpTotalIncoming.incrementAndGet();
    }
    public static void incrementHttpRequestOutgoing(){
        httpTotalOutgoing.incrementAndGet();
    }
    public static void incrementHttp200(){
        http200.incrementAndGet();
    }
    public static void incrementHttp400(){
        http400.incrementAndGet();
    }
    public static void incrementHttp500(){
        http500.incrementAndGet();
    }

    public static void incrementIpRateLimitTrigger(){
        ipRateLimitTriggers.incrementAndGet();
    }
    public static void incrementLoginRateLimitTrigger(){
        loginRateLimitTriggers.incrementAndGet();
    }
    public static void incrementLoginSuccess(){
        loginSuccess.incrementAndGet();
    }
    public static void incrementLoginError(){
        loginError.incrementAndGet();
    }

    public static void incrementCustomerQueriesSuccess(){
        customerQueriesSuccess.incrementAndGet();
    }
    public static void incrementCustomerMutationsSuccess(){
        customerMutationsSuccess.incrementAndGet();
    }
    public static void incrementPurchaseOrdersSuccess(){
        purchaseOrdersSuccess.incrementAndGet();
    }
    public static void incrementStaffQueriesSuccess(){
        staffQueriesSuccess.incrementAndGet();
    }
    public static void incrementStaffMutationsSuccess(){
        staffMutationsSuccess.incrementAndGet();
    }

    public static void incrementCustomerQueriesError(){
        customerQueriesError.incrementAndGet();
    }
    public static void incrementCustomerMutationsError(){
        customerMutationsError.incrementAndGet();
    }
    public static void incrementStaffQueriesError(){
        staffQueriesError.incrementAndGet();
    }
    public static void incrementStaffMutationsError(){
        staffMutationsError.incrementAndGet();
    }
}
