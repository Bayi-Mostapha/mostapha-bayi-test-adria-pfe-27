package com.bank.accountservice.service;

import java.util.UUID;

/**
 * Outcome of a transfer: the two accounts, the moved amount and both balances after it.
 */
public record TransferResult(
        UUID fromAccountId,
        UUID toAccountId,
        double amount,
        String currency,
        double fromBalance,
        double toBalance) {
}
