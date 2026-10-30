package com.bank.accountservice.web.rest.dto;

import com.bank.accountservice.domain.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Body of POST /api/accounts. The status is optional and defaults to ACTIVE.
 */
public record AccountCreateRequest(
        @NotBlank String customerReference,
        @NotBlank String currency,
        @PositiveOrZero double balance,
        AccountStatus status) {
}
