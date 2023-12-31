/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.authorizationcommon;

import com.fjellsoftware.javafunctionalutils.ImmutableList;
import com.fjellsoftware.javafunctionalutils.opt.Opt;
import com.fjellsoftware.retaildemo.authorizationcommon.userinfo.AuthenticatedUser;
import com.fjellsoftware.retaildemo.authorizationcommon.userinfo.UserInfo;
import com.fjellsoftware.retaildemo.autogenerated.orm.*;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.loppi.includablevalues.UUIDValue;
import io.loppi.orm.query.DatabaseQuery;
import io.loppi.orm.PostgresExecutionException;
import io.loppi.includablevalues.AssociationValue;
import io.loppi.includablevalues.StringValue;


import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LoginSessionService {

    private final Cache<UUID, UserInfo> userTokenCache;
    private final DatabaseServiceJDBCRetailDemo databaseServiceJDBC;
    private final UserAccountMeta userAccountMeta;
    private final LoginSessionMeta userSessionMeta;

    public LoginSessionService(DatabaseServiceJDBCRetailDemo databaseServiceJDBC, MetaRetailDemo meta) {
        this.databaseServiceJDBC = databaseServiceJDBC;
        this.userAccountMeta = meta.getUserAccountMeta();
        this.userSessionMeta = meta.getLoginSessionMeta();
        this.userTokenCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(100_000)
                .build();
    }

    public Opt<UserInfo> getUserInfoFromCacheOrFetch(
            UUID sessionToken, InetAddress remoteAddress) throws PostgresExecutionException {
        UserInfo fromCacheOrNull = userTokenCache.getIfPresent(sessionToken);
        if(fromCacheOrNull != null) {
            return Opt.of(fromCacheOrNull);
        }
        DatabaseQuery<LoginSessionResult> query = databaseServiceJDBC.createQueryBuilder(LoginSessionResult.class, 1)
                .includeOwnerAttribute(userSessionMeta.userAccount)
                    .includeScalar1(userAccountMeta.userAccountId)
                    .includeScalar1(userAccountMeta.username)
                    .includeScalar1(userAccountMeta.role)
                .end1()
                .where()
                .eq(userSessionMeta.loginSessionId, sessionToken)
                .eq(userSessionMeta.isSignedOut, false)
                .gt(userSessionMeta.createdAt, OffsetDateTime.now().minusHours(25))
                .end().build();
        ImmutableList<LoginSessionResult> userSessions = databaseServiceJDBC.executeQuery(query).deserialize();
        if(userSessions.isEmpty()){
            return Opt.empty();
        }
        LoginSessionResult userSession = userSessions.iterator().next();
        UserAccountResult user = ((AssociationValue<UserAccountResult>) userSession.getUserAccount()).getValue();
        UUID userId = ((UUIDValue) user.getUserAccountId()).getValue();
        String username = ((StringValue) user.getUsername()).getValue();
        String roleName = ((StringValue) user.getRole()).getValue();
        RoleEnum userInfoRole = RoleEnum.valueOf(roleName);
        UserInfo userInfo = new AuthenticatedUser(userId, username, userInfoRole, sessionToken, remoteAddress);
        userTokenCache.put(sessionToken, userInfo);
        return Opt.of(userInfo);
    }

    public void invalidateSession(UUID sessionToken){
        userTokenCache.invalidate(sessionToken);
    }
}
