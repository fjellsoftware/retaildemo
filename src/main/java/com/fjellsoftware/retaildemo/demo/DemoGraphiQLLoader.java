/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fjellsoftware.javafunctionalutils.opt.Opt;
import com.fjellsoftware.javafunctionalutils.opt.Some;
import com.fjellsoftware.retaildemo.ApplicationConfiguration;
import com.fjellsoftware.retaildemo.ApplicationInternalException;
import com.fjellsoftware.retaildemo.authorizationcommon.RateLimiter;
import com.fjellsoftware.retaildemo.util.InetAddressUtils;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.*;

public class DemoGraphiQLLoader {

    private final Opt<String> customerGraphiQLHTML;
    private final Opt<String> staffGraphiQLHTML;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RateLimiter rateLimiter;
    private final ApplicationConfiguration applicationConfiguration;

    public DemoGraphiQLLoader(RateLimiter rateLimiter, String hostName, ApplicationConfiguration applicationConfiguration) {
        Opt<String> graphiQLHTMLTemplate = initializeGraphiQLHTMLFileString();
        if(!(graphiQLHTMLTemplate instanceof Some<String> some)){
            this.customerGraphiQLHTML = Opt.empty();
            this.staffGraphiQLHTML = Opt.empty();
        }
        else {
            String template = some.value();
            this.customerGraphiQLHTML = Opt.of(initializeCustomerGraphiQLHTML(template, hostName));
            this.staffGraphiQLHTML = Opt.of(initializeStaffGraphiQLHTML(template, hostName));
        }
        this.rateLimiter = rateLimiter;
        this.applicationConfiguration = applicationConfiguration;
    }

    private String initializeCustomerGraphiQLHTML(String htmlTemplate, String protocolAndDomainBaseUrl){
        Map<String,Map<String,Object>> variablesByFileName = new HashMap<>();
        LinkedHashMap<String,Object> loginVariables = new LinkedHashMap<>();
        loginVariables.put("username", "[your_username]");
        loginVariables.put("password", "[your_password]");
        variablesByFileName.put("login.graphql", loginVariables);

        Map<String,Object> buyAnimalStickersVariables = new HashMap<>();
        variablesByFileName.put("buyAnimalStickers.graphql", buyAnimalStickersVariables);

        Map<String,Object> placeOrderVariables = new HashMap<>();
        Map<String,Object> orderToInsert = new HashMap<>();
        orderToInsert.put("countryId", 9);
        List<Map<String,Object>> orderLinesToInsert = new ArrayList<>();
        Map<String,Object> orderLineToInsert = new HashMap<>();
        orderLineToInsert.put("productId", 1);
        orderLineToInsert.put("unitPrice", "0.85");
        orderLineToInsert.put("quantity", 1);
        orderLinesToInsert.add(orderLineToInsert);
        orderToInsert.put("orderLinesToInsert", orderLinesToInsert);
        placeOrderVariables.put("orderToInsert", orderToInsert);
        variablesByFileName.put("placeOrder.graphql", placeOrderVariables);

        return formatHTMLTemplate(htmlTemplate, Path.of("graphiQLInitialQueries", "customer"),
                variablesByFileName, protocolAndDomainBaseUrl + "/api/v1/graphql");
    }

    private String initializeStaffGraphiQLHTML(String htmlTemplate, String protocolAndDomainBaseUrl){
        Map<String,Map<String,Object>> variablesByFileName = new HashMap<>();

        Map<String,Object> markOrderShippedVariables = new HashMap<>();
        markOrderShippedVariables.put("purchaseOrderId", 0);
        variablesByFileName.put("markOrderShipped.graphql", markOrderShippedVariables);

        Map<String,Object> productUpdateVariables = new HashMap<>();
        Map<String,Object> productToUpdate = new HashMap<>();
        productToUpdate.put("whereProductId", 1);
        productToUpdate.put("currentUnitPrice", "4.45");
        productToUpdate.put("description", "LARGE INFLATABLE ANIMAL");
        productUpdateVariables.put("productToUpdate", productToUpdate);
        variablesByFileName.put("productUpdate.graphql", productUpdateVariables);

        Map<String,Object> productInsertVariables = new HashMap<>();
        Map<String,Object> productToInsert = new HashMap<>();
        productToInsert.put("currentUnitPrice", "1.45");
        productToInsert.put("description", "LARGE INFLATABLE ANIMAL");
        productInsertVariables.put("productToInsert", productToInsert);
        variablesByFileName.put("productInsert.graphql", productInsertVariables);

        return formatHTMLTemplate(htmlTemplate, Path.of("graphiQLInitialQueries", "staff"),
                variablesByFileName, protocolAndDomainBaseUrl + "/api/v1/staff/graphql");
    }

    private String formatHTMLTemplate(String htmlTemplateToFormat, Path resourceQueryFilesPath,
                                      Map<String,Map<String,Object>> variablesByFileName, String url){
        try {
            String htmlWithUrl = htmlTemplateToFormat.replace("url: ''", String.format("url: '%s'", url));
            Class<? extends DemoGraphiQLLoader> aClass = this.getClass();
            String path = Path.of(resourceQueryFilesPath.toString(), "initialQueriesFileNames").toString();
            InputStream fileNamesStream = aClass.getResourceAsStream("/"+path);
            StringBuilder sb = new StringBuilder();
            boolean anyQueries = false;
            if(fileNamesStream != null){
                List<String> queryFileNames = new ArrayList<>();
                Scanner fileNamesScanner = new Scanner(fileNamesStream);
                while (fileNamesScanner.hasNextLine()) {
                    queryFileNames.add(fileNamesScanner.nextLine());
                }
                List<Map<String,Object>> queries = new ArrayList<>();
                sb.append("initialTabs: ");
                for (String queryFileName : queryFileNames) {

                    StringBuilder queryBuilder = new StringBuilder();
                    try (InputStream initialQueryFileStream = aClass.getResourceAsStream(
                            "/" + Path.of(resourceQueryFilesPath.toString(), queryFileName).toString())){
                        if(initialQueryFileStream == null){
                            continue;
                        }
                        anyQueries = true;
                        Scanner scanner = new Scanner(initialQueryFileStream);
                        while (scanner.hasNextLine()) {
                            queryBuilder.append(scanner.nextLine()).append("\n");
                        }
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("query", queryBuilder.toString());
                    queries.add(map);
                    Map<String, Object> variablesForQueryNullable = variablesByFileName.get(queryFileName);
                    if(variablesForQueryNullable != null){
                        map.put("variables", objectMapper.writeValueAsString(variablesForQueryNullable));
                    }
                }
                sb.append(objectMapper.writeValueAsString(queries));
                sb.append(",");
            }
            if(anyQueries){
                return htmlWithUrl.replace("initialTabs: [],", sb.toString());
            }
            return htmlWithUrl.replace("initialTabs: [],", "");
        }catch (IOException | RuntimeException e) {
            throw new ApplicationInternalException("Failed to load initial tab query files for GraphiQL", e);
        }
    }

    private Opt<String> initializeGraphiQLHTMLFileString(){
        InputStream resourceFileStream =
                DemoGraphiQLLoader.class.getClassLoader().getResourceAsStream("graphiql.html");
        if(resourceFileStream == null){
            return Opt.empty();
        }
        Scanner scanner = new Scanner(resourceFileStream);
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine()) {
            sb.append(scanner.nextLine());
        }
        return Opt.of(sb.toString());
    }

    private void httpGetGraphiQLCommon(Context ctx, Opt<String> htmlOpt, ApplicationConfiguration applicationConfiguration){
        InetAddress remoteAddress = InetAddressUtils.extractRemoteFromContext(ctx, applicationConfiguration);
        boolean didConsume = rateLimiter.tryConsumeRegular(remoteAddress, 1);
        if(!didConsume){
            ctx.html("Too many requests.");
            ctx.status(HttpStatus.TOO_MANY_REQUESTS);
            return;
        }
        String accept = ctx.headerMap().get("Accept");
        if(accept == null || !accept.contains(ContentType.HTML)){
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }
        if(!(htmlOpt instanceof Some<String> some)){
            ctx.status(HttpStatus.NOT_FOUND);
        }
        else{
            ctx.html(some.value());
            ctx.status(HttpStatus.OK);
        }

    }
    public void httpGetCustomerGraphiQL(Context ctx){
        httpGetGraphiQLCommon(ctx, customerGraphiQLHTML, applicationConfiguration);
    }

    public void httpGetStaffGraphiQL(Context ctx){
        httpGetGraphiQLCommon(ctx, staffGraphiQLHTML, applicationConfiguration);
    }
}
