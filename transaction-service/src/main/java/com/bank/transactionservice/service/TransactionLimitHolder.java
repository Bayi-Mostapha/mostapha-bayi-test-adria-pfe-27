package com.bank.transactionservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Holds bank.transaction.max-amount coming from the config-service. The bean is
 * @RefreshScope, so a /actuator/refresh pulls the new limit without restarting.
 */
@Component
@RefreshScope
public class TransactionLimitHolder {

    @Value("${bank.transaction.max-amount}")
    private double maxAmount;

    public double getMaxAmount() {
        return maxAmount;
    }
}
