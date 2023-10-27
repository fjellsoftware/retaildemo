/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.authorizationcommon;

import com.fjellsoftware.retaildemo.Metrics;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.bucket4j.local.LocalBucket;

import java.math.BigInteger;
import java.net.InetAddress;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class RateLimiter {

    private static final GraphQLRateLimitException rateLimitException =
            new GraphQLRateLimitException();
    private final Cache<Long, LocalBucket> regularRateLimitBucketsByIp;
    private final Cache<String, LocalBucket> loginRateLimitBucketsByCaptcha;
    private final boolean isEnabled;

    public RateLimiter(boolean isEnabled) {
        this.isEnabled = isEnabled;
        regularRateLimitBucketsByIp = Caffeine.newBuilder()
                .maximumSize(2_000_000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
        loginRateLimitBucketsByCaptcha = Caffeine.newBuilder()
                .maximumSize(2_000_000)
                .expireAfterWrite(4, TimeUnit.HOURS)
                .build();
    }

    public static GraphQLRateLimitException getRateLimitException(){
        return rateLimitException;
    }

    // If our users were actual staff members or paying customers instead of demo users, then we would have added tiers
    // of users and different rate-limits based on user tier.
    public boolean tryConsumeRegular(InetAddress remoteAddress, int tokens){
        if(!isEnabled){
            return true;
        }
        long addressLong = mostSignificantLongFromIp(remoteAddress);
        LocalBucket bucketForUser = regularRateLimitBucketsByIp.get(addressLong, this::createNewRegularBucket);
        boolean succeeded = bucketForUser.tryConsume(tokens);
        if(!succeeded){
            Metrics.incrementIpRateLimitTrigger();
        }
        return succeeded;
    }

    public void consumeRegularGraphQL(InetAddress remoteAddress, int tokens) throws GraphQLRateLimitException {
        if(!isEnabled){
            return;
        }
        boolean wasConsumed = tryConsumeRegular(remoteAddress, tokens);
        if(!wasConsumed){
            throw getRateLimitException();
        }
    }

    private LocalBucket createNewRegularBucket(long addressLong){
        return Bucket.builder().addLimit(Bandwidth.classic(30, Refill.greedy(1, Duration.ofSeconds(1)))).build();
    }

    public boolean checkCanConsumeLoginGraphQL(String captchaToken){
        if(!isEnabled){
            return true;
        }
        LocalBucket bucketForUser = loginRateLimitBucketsByCaptcha.get(captchaToken, this::createNewLoginBucket);
        boolean succeeded = bucketForUser.getAvailableTokens() > 0;
        if(!succeeded){
            Metrics.incrementLoginRateLimitTrigger();
        }
        return succeeded;
    }

    public void consumeLoginFailedGraphQL(InetAddress remoteAddress, String captchaToken){
        if(!isEnabled){
            return;
        }
        LocalBucket bucketForUser = loginRateLimitBucketsByCaptcha.get(captchaToken, this::createNewLoginBucket);
        boolean wasConsumed = bucketForUser.tryConsume(1);
        if(!wasConsumed){
            bucketForUser.tryConsumeAsMuchAsPossible();
        }
        long addressLong = mostSignificantLongFromIp(remoteAddress);
        LocalBucket regularBucketForUser = regularRateLimitBucketsByIp.get(addressLong, this::createNewRegularBucket);
        regularBucketForUser.consumeIgnoringRateLimits(6);
    }

    private LocalBucket createNewLoginBucket(String captchaToken){
        return Bucket.builder().addLimit(Bandwidth.classic(4, Refill.greedy(1, Duration.ofSeconds(20)))).build();
    }

    private long mostSignificantLongFromIp(InetAddress remoteAddress){
        byte[] addressBytes = remoteAddress.getAddress();
        // Create long value based on the address bytes, maximum 8. So the full ip4 is used and the first half of ipv6.
        return new BigInteger(addressBytes, 0, Math.min(addressBytes.length, 8)).longValue();
    }
}
