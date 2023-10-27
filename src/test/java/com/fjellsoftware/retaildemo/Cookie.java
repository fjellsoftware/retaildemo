/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

public record Cookie(String cookieName, String value) {
    public String headerName(){
        return "cookie";
    }

    public String headerValue(){
        return cookieName + "=" + value;
    }
}
