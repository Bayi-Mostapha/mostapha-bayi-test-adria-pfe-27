package com.bank.accountservice.domain;

/**
 * Raised when a business rule of the account-service is broken: blocked account,
 * currency mismatch, non positive amount or a transfer onto itself. Mapped to HTTP 409.
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}
