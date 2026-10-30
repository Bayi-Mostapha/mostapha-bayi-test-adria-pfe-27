package com.bank.transactionservice.service;

import com.bank.transactionservice.domain.AccountNotActiveException;
import com.bank.transactionservice.domain.AccountNotFoundException;
import com.bank.transactionservice.domain.CurrencyMismatchException;
import com.bank.transactionservice.domain.InsufficientBalanceException;
import com.bank.transactionservice.domain.Transaction;
import com.bank.transactionservice.domain.TransactionLimitExceededException;
import com.bank.transactionservice.domain.TransactionNotFoundException;
import com.bank.transactionservice.repository.TransactionRepository;
import com.bank.transactionservice.web.rest.dto.TransactionCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * All business logic of the transaction-service. The rules run in this exact order:
 * amount &gt; 0, amount within the limit, account exists, account ACTIVE,
 * same currency, enough balance, save, debit the account, publish the event.
 * Controllers contain none of it.
 */
@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AccountGateway accountGateway;
    private final TransactionEventPublisher eventPublisher;
    private final TransactionLimitHolder limitHolder;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountGateway accountGateway,
                              TransactionEventPublisher eventPublisher,
                              TransactionLimitHolder limitHolder) {
        this.transactionRepository = transactionRepository;
        this.accountGateway = accountGateway;
        this.eventPublisher = eventPublisher;
        this.limitHolder = limitHolder;
    }

    @Transactional
    public Transaction create(TransactionCreateRequest request) {
        if (request.amount().signum() <= 0) {
            throw new IllegalArgumentException("Amount must be strictly greater than zero, got " + request.amount());
        }
        BigDecimal maxAmount = BigDecimal.valueOf(limitHolder.getMaxAmount());
        if (request.amount().compareTo(maxAmount) > 0) {
            throw new TransactionLimitExceededException(request.amount(), maxAmount);
        }

        AccountSnapshot account = accountGateway.getAccount(request.accountId());
        if (account == null) {
            throw new AccountNotFoundException(request.accountId());
        }
        if (!"ACTIVE".equals(account.status())) {
            throw new AccountNotActiveException(account.id(), account.status());
        }
        if (!account.currency().equals(request.currency())) {
            throw new CurrencyMismatchException(account.id(), account.currency(), request.currency());
        }
        if (account.balance() < request.amount().doubleValue()) {
            throw new InsufficientBalanceException(account.id(), account.balance(), request.amount());
        }

        Transaction transaction = new Transaction();
        transaction.setAccountId(request.accountId());
        transaction.setAmount(request.amount());
        transaction.setCurrency(request.currency());
        transaction.setMerchantCategory(request.merchantCategory());
        transaction.setCountry(request.country());
        Transaction saved = transactionRepository.save(transaction);

        accountGateway.debit(request.accountId(), request.amount());
        eventPublisher.publish(saved);

        log.info("Created transaction {} of {} {} on account {}",
                saved.getId(), saved.getAmount(), saved.getCurrency(), saved.getAccountId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Transaction findById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByAccountId(UUID accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @Transactional(readOnly = true)
    public TransactionSummary summary(UUID accountId) {
        List<Transaction> transactions = findByAccountId(accountId);
        BigDecimal total = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TransactionSummary(transactions.size(), total);
    }
}
