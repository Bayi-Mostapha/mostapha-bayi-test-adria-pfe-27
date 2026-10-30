package com.bank.transactionservice.domain;

import java.util.UUID;

/**
 * The account is BLOCKED and cannot be debited. Mapped to HTTP 409.
 */
public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(UUID accountId, String status) {
        super("Account " + accountId + " is " + status + " and cannot receive transactions");
    }
}
