package com.bank.transactionservice.domain;

import java.util.UUID;

/**
 * No transaction with that id. Mapped to HTTP 404.
 */
public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(UUID id) {
        super("Transaction not found: " + id);
    }
}
