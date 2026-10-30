package com.bank.transactionservice.service;

import java.math.BigDecimal;

/**
 * Aggregated view of the transactions of one account.
 */
public record TransactionSummary(long count, BigDecimal totalAmount) {
}
