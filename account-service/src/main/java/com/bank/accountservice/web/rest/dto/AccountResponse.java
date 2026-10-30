package com.bank.accountservice.web.rest.dto;

import com.bank.accountservice.domain.AccountStatus;

import java.util.UUID;

public record AccountResponse(
        UUID id,
        String customerReference,
        AccountStatus status,
        String currency,
        double balance) {
}
