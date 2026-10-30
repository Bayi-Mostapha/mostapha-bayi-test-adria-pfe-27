package com.bank.accountservice.domain;

/**
 * Lifecycle of an account. A BLOCKED account is frozen: no debit, credit or transfer.
 */
public enum AccountStatus {
    ACTIVE,
    BLOCKED
}
