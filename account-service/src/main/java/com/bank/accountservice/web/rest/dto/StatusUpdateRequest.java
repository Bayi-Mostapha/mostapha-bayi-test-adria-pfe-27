package com.bank.accountservice.web.rest.dto;

import com.bank.accountservice.domain.AccountStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Body of PATCH /api/accounts/{id}/status.
 */
public record StatusUpdateRequest(@NotNull AccountStatus status) {
}
