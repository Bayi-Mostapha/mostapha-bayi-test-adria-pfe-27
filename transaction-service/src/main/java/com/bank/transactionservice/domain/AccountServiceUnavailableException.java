package com.bank.transactionservice.domain;

/**
 * account-service could not be reached or answered with an error.
 * Mapped to HTTP 503 so the caller knows the failure is temporary.
 */
public class AccountServiceUnavailableException extends RuntimeException {

    public AccountServiceUnavailableException(String message) {
        super(message);
    }
}
