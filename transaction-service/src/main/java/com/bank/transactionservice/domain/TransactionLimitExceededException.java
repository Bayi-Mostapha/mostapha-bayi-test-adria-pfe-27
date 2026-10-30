package com.bank.transactionservice.domain;

import java.math.BigDecimal;

/**
 * The amount is above bank.transaction.max-amount. Mapped to HTTP 409.
 */
public class TransactionLimitExceededException extends RuntimeException {

    public TransactionLimitExceededException(BigDecimal amount, BigDecimal maxAmount) {
        super("Transaction amount " + amount + " exceeds the limit of " + maxAmount);
    }
}
