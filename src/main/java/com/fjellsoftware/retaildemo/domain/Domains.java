/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.domain;

import com.fjellsoftware.retaildemo.authorizationcommon.HCaptchaCache;
import com.fjellsoftware.retaildemo.authorizationcommon.RateLimiter;
import com.fjellsoftware.retaildemo.authorizationcommon.LoginSessionService;
import com.fjellsoftware.retaildemo.autogenerated.orm.main.RetailDemoORMService;
import com.fjellsoftware.retaildemo.autogenerated.orm.main.RetailDemoMeta;
import com.fjellsoftware.retaildemo.autogenerated.graphql.customer.CustomerGraphQLService;
import com.fjellsoftware.retaildemo.demo.DemoDomain;

import java.security.MessageDigest;

public class Domains {
    private final ProductDomain productDomain;
    private final UserDomain userdomain;
    private final OrderDomain orderDomain;
    private final LoginSessionDomain loginSessionDomain;
    private final DemoDomain demoDomain;

    public Domains(
            RetailDemoORMService loppiService, LoginSessionService loginSessionService, RateLimiter rateLimiter,
            HCaptchaCache hCaptchaCache, MessageDigest sha256Digest, CustomerGraphQLService customerGraphQLService,
            String hostName) {
        RetailDemoMeta metaRetailDemo = loppiService.getMeta();
        this.productDomain = new ProductDomain(metaRetailDemo.getProductMeta(), loppiService);
        this.userdomain = new UserDomain(loppiService, metaRetailDemo.getUserAccountMeta(),
                hCaptchaCache, sha256Digest, customerGraphQLService, hostName);
        this.orderDomain = new OrderDomain(loppiService);
        this.loginSessionDomain = new LoginSessionDomain(
                loppiService, loginSessionService, rateLimiter, sha256Digest, customerGraphQLService);
        this.demoDomain = new DemoDomain(loginSessionService, customerGraphQLService);
    }

    public ProductDomain getProductDomain() {
        return productDomain;
    }

    public UserDomain getUserdomain() {
        return userdomain;
    }

    public OrderDomain getOrderDomain() {
        return orderDomain;
    }

    public LoginSessionDomain getLoginSessionDomain() {
        return loginSessionDomain;
    }

    public DemoDomain getDemoDomain() {
        return demoDomain;
    }
}
