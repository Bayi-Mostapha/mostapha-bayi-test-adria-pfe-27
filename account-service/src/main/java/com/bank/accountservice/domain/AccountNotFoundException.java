package com.bank.accountservice.domain;

import java.util.UUID;

/**
 * Raised when an account cannot be found. Mapped to HTTP 404.
 */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(UUID id) {
        super("Account not found: " + id);
    }

    public AccountNotFoundException(String message) {
        super(message);
    }
}
