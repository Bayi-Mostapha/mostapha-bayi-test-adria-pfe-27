package com.bank.transactionservice.domain;

import java.util.UUID;

/**
 * account-service answered that the account does not exist. Mapped to HTTP 404.
 */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(UUID accountId) {
        super("Account not found: " + accountId);
    }
}
