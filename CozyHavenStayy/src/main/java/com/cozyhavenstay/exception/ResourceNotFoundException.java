package com.cozyhavenstay.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // This sets the default HTTP status for this exception
public class ResourceNotFoundException extends RuntimeException {

    // Constructor that takes only a message (the one you already had)
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // --- ADD THIS NEW CONSTRUCTOR ---
    // This constructor allows you to pass the original exception (cause)
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}