/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

import java.util.UUID;

record TestCredentials(String username, String password, UUID sessionToken, UUID userAccountId) {
}
