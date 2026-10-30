package com.bank.accountservice.domain;

import java.util.UUID;

/**
 * Raised when an account does not carry enough balance. Mapped to HTTP 409.
 */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(UUID accountId, double balance, double amount) {
        super("Insufficient funds on account " + accountId
                + ": balance is " + balance + " but " + amount + " was requested");
    }
}
