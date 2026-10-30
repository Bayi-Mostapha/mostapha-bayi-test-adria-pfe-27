package com.bank.transactionservice.service;

import com.bank.transactionservice.domain.Transaction;

/**
 * Publishes the bank.transactions.created event. The Kafka implementation comes later,
 * the service only depends on this interface.
 */
public interface TransactionEventPublisher {

    void publish(Transaction transaction);
}
