package com.bank.transactionservice.service;

import java.util.UUID;

/**
 * Account as seen by transaction-service: the JSON returned by account-service.
 */
public record AccountSnapshot(
        UUID id,
        String customerReference,
        String status,
        String currency,
        double balance) {
}
