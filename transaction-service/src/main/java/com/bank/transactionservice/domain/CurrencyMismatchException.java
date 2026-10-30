package com.bank.transactionservice.domain;

import java.util.UUID;

/**
 * The transaction currency differs from the account currency. Mapped to HTTP 409.
 */
public class CurrencyMismatchException extends RuntimeException {

    public CurrencyMismatchException(UUID accountId, String accountCurrency, String transactionCurrency) {
        super("Currency mismatch: account " + accountId + " is in " + accountCurrency
                + " but the transaction is in " + transactionCurrency);
    }
}
