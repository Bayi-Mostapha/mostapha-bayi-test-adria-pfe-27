package com.bank.transactionservice.web.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Body of POST /api/transactions. The service re-checks every rule, Bean Validation only
 * rejects malformed bodies early with 400.
 */
public record TransactionCreateRequest(
        @NotNull UUID accountId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String merchantCategory,
        @NotBlank String country) {
}
