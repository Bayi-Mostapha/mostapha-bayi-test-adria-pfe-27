package com.bank.transactionservice.service;

import com.bank.transactionservice.domain.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Placeholder until Kafka is wired: it only logs what would have been published.
 */
@Component
public class NoOpTransactionEventPublisher implements TransactionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpTransactionEventPublisher.class);

    @Override
    public void publish(Transaction transaction) {
        log.info("Kafka is not wired yet, event bank.transactions.created was not published for transaction {} on account {}",
                transaction.getId(), transaction.getAccountId());
    }
}
