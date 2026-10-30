package com.bank.transactionservice.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The account does not hold enough money. Mapped to HTTP 409.
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(UUID accountId, double balance, BigDecimal amount) {
        super("Insufficient balance on account " + accountId
                + ": balance is " + balance + " but " + amount + " was requested");
    }
}
