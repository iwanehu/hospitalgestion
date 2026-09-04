package com.hospital.gestion.api.common.exception;

public class LoginRateLimitException
        extends RuntimeException {

    public LoginRateLimitException(String message) {
        super(message);
    }
}

