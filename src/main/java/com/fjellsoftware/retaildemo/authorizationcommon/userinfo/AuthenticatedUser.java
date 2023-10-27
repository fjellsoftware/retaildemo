/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.authorizationcommon.userinfo;

import com.fjellsoftware.retaildemo.authorizationcommon.RoleEnum;

import java.net.InetAddress;
import java.util.UUID;

public record AuthenticatedUser(UUID userId, String username, RoleEnum role, UUID sessionToken,
                                InetAddress remoteAddress) implements UserInfo {

}
