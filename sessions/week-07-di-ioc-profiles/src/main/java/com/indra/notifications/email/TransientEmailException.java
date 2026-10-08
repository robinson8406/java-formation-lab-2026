package com.indra.notifications.email;

public class TransientEmailException extends RuntimeException {

    public TransientEmailException(String message) {
        super(message);
    }

    public TransientEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}