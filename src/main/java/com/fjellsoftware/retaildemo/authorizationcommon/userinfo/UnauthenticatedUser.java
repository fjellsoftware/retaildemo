/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo.authorizationcommon.userinfo;

import java.net.InetAddress;

public record UnauthenticatedUser(InetAddress remoteAddress) implements UserInfo {
}
