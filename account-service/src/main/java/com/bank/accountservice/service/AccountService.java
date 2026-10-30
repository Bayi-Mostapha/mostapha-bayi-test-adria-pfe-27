package com.bank.accountservice.service;

import com.bank.accountservice.domain.Account;
import com.bank.accountservice.domain.AccountNotFoundException;
import com.bank.accountservice.domain.AccountStatus;
import com.bank.accountservice.domain.InsufficientFundsException;
import com.bank.accountservice.domain.InvalidOperationException;
import com.bank.accountservice.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * All business validation of the account-service lives here. REST controllers, GraphQL
 * controllers and MCP tools call this class and contain no business logic themselves.
 */
@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account create(String customerReference, String currency, double balance, AccountStatus status) {
        Account account = new Account();
        account.setCustomerReference(customerReference);
        account.setCurrency(currency == null ? "EUR" : currency.trim().toUpperCase());
        account.setBalance(balance);
        account.setStatus(status == null ? AccountStatus.ACTIVE : status);
        Account saved = accountRepository.save(account);
        log.info("Created account {} for customer {} with balance {} {}",
                saved.getId(), saved.getCustomerReference(), saved.getBalance(), saved.getCurrency());
        return saved;
    }

    @Transactional(readOnly = true)
    public Account findById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Account findByCustomerReference(String customerReference) {
        return accountRepository.findByCustomerReference(customerReference)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for customer: " + customerReference));
    }

    @Transactional
    public Account updateStatus(UUID id, AccountStatus status) {
        if (status == null) {
            throw new InvalidOperationException("Account status must not be null");
        }
        Account account = findById(id);
        account.setStatus(status);
        Account saved = accountRepository.save(account);
        log.info("Account {} status set to {}", id, status);
        return saved;
    }

    @Transactional(readOnly = true)
    public double getBalance(UUID id) {
        return findById(id).getBalance();
    }

    @Transactional
    public Account debit(UUID id, double amount) {
        requirePositiveAmount(amount);
        Account account = findById(id);
        requireActive(account);
        if (account.getBalance() < amount) {
            throw new InsufficientFundsException(id, account.getBalance(), amount);
        }
        account.setBalance(account.getBalance() - amount);
        Account saved = accountRepository.save(account);
        log.info("Debited {} from account {}, new balance {}", amount, id, saved.getBalance());
        return saved;
    }

    @Transactional
    public Account credit(UUID id, double amount) {
        requirePositiveAmount(amount);
        Account account = findById(id);
        requireActive(account);
        account.setBalance(account.getBalance() + amount);
        Account saved = accountRepository.save(account);
        log.info("Credited {} to account {}, new balance {}", amount, id, saved.getBalance());
        return saved;
    }

    /**
     * Moves the amount atomically: both accounts must be ACTIVE, share the same currency
     * and the sender must hold enough funds. The whole method runs in one transaction, so
     * either both balances change or none does.
     */
    @Transactional
    public TransferResult transfer(UUID fromId, UUID toId, double amount) {
        requirePositiveAmount(amount);
        if (fromId.equals(toId)) {
            throw new InvalidOperationException("A transfer must use two different accounts");
        }
        Account from = findById(fromId);
        Account to = findById(toId);
        requireActive(from);
        requireActive(to);
        if (!from.getCurrency().equals(to.getCurrency())) {
            throw new InvalidOperationException("Currency mismatch: account " + fromId + " is in "
                    + from.getCurrency() + " but account " + toId + " is in " + to.getCurrency());
        }
        if (from.getBalance() < amount) {
            throw new InsufficientFundsException(fromId, from.getBalance(), amount);
        }
        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);
        accountRepository.save(from);
        accountRepository.save(to);
        log.info("Transferred {} {} from account {} to account {}", amount, from.getCurrency(), fromId, toId);
        return new TransferResult(fromId, toId, amount, from.getCurrency(), from.getBalance(), to.getBalance());
    }

    private void requirePositiveAmount(double amount) {
        if (amount <= 0) {
            throw new InvalidOperationException("Amount must be strictly greater than zero, got " + amount);
        }
    }

    private void requireActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidOperationException("Account " + account.getId() + " is " + account.getStatus()
                    + " and cannot be used for this operation");
        }
    }
}
