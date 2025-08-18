package com.furkancavdar.qrmenu.auth.application.exception;

public class SessionOwnerException extends RuntimeException {
    public SessionOwnerException(String message) {
        super(message);
    }
}
