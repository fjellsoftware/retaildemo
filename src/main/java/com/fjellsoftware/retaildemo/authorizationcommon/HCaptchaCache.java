/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.authorizationcommon;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

public class HCaptchaCache {
    private final Cache<String, Boolean> tokenCache_hasSignedUp;

    public HCaptchaCache() {
        this.tokenCache_hasSignedUp = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(100_000)
                .build();
    }

    public void registerValidToken(String token){
        tokenCache_hasSignedUp.put(token, false);
    }

    public boolean checkTokenPresent(String token){
        if(token.length() > 100){
            return false;
        }
        return tokenCache_hasSignedUp.getIfPresent(token) != null;
    }

    public boolean checkTokenValidForSignUp(String token){
        if(token.length() > 100){
            return false;
        }
        Boolean wasUsedForSignUpNullable = tokenCache_hasSignedUp.getIfPresent(token);
        if(wasUsedForSignUpNullable == null){
            return false;
        }
        return !wasUsedForSignUpNullable;
    }

    public void consumeTokenForSignUp(String token){
        Boolean wasUsedForSignUpNullable = tokenCache_hasSignedUp.getIfPresent(token);
        if(wasUsedForSignUpNullable == null){
            return;
        }
        tokenCache_hasSignedUp.put(token, true);
    }
}
