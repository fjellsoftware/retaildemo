/*
 * © 2023. This work is openly licensed via CC0 1.0.
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

package com.fjellsoftware.retaildemo;

public class ApplicationInternalException extends RuntimeException {
    public ApplicationInternalException(String message){
        super(message);
    }

    public ApplicationInternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
