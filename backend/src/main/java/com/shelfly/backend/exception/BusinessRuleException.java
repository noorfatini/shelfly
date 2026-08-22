package com.shelfly.backend.exception;

/**
 * Thrown when a request is well-formed but violates a domain business rule,
 * e.g. borrowing a book with zero available copies, or duplicate registration.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
