/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.util;

import com.fjellsoftware.retaildemo.ApplicationConfiguration;
import com.fjellsoftware.retaildemo.ApplicationInternalException;
import io.javalin.http.Context;
import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressUtils {
    public static InetAddress extractRemoteFromContext(Context ctx, ApplicationConfiguration applicationConfiguration){

        String remoteAddr;
        if(applicationConfiguration.isProduction()) {
            remoteAddr = ctx.header("x-forwarded-for");
        }
        else{
            HttpServletRequest httpServletRequest = ctx.req();
            remoteAddr = httpServletRequest.getRemoteAddr();
        }
        InetAddress remoteAddress;
        try {
            remoteAddress = InetAddress.getByName(remoteAddr);
        } catch (UnknownHostException e) {
            throw new ApplicationInternalException("Failed to process the request address.", e);
        }
        return remoteAddress;
    }
}
