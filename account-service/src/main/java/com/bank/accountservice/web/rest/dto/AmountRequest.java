package com.bank.accountservice.web.rest.dto;

import jakarta.validation.constraints.Positive;

/**
 * Body of POST /api/accounts/{id}/debit and POST /api/accounts/{id}/credit.
 */
public record AmountRequest(@Positive double amount) {
}
