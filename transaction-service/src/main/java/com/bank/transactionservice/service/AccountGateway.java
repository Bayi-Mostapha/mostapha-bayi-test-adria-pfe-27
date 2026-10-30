package com.bank.transactionservice.service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Port used by the service layer to reach account-service. Today it is implemented with a
 * load balanced RestClient, Feign will replace the implementation in the next step without
 * touching TransactionService.
 */
public interface AccountGateway {

    AccountSnapshot getAccount(UUID accountId);

    AccountSnapshot debit(UUID accountId, BigDecimal amount);
}
