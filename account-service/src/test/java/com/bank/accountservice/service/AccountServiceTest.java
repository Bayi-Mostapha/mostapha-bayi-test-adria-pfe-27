package com.bank.accountservice.service;

import com.bank.accountservice.domain.Account;
import com.bank.accountservice.domain.AccountStatus;
import com.bank.accountservice.domain.InsufficientFundsException;
import com.bank.accountservice.domain.InvalidOperationException;
import com.bank.accountservice.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests of the business rules of AccountService.transfer, with a mocked repository.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    private UUID fromId;
    private UUID toId;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository);
        fromId = UUID.randomUUID();
        toId = UUID.randomUUID();
    }

    @Test
    void transferMovesFundsBetweenTwoActiveAccounts() {
        Account from = account(fromId, AccountStatus.ACTIVE, "EUR", 5000);
        Account to = account(toId, AccountStatus.ACTIVE, "EUR", 3000);
        when(accountRepository.findById(fromId)).thenReturn(Optional.of(from));
        when(accountRepository.findById(toId)).thenReturn(Optional.of(to));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransferResult result = accountService.transfer(fromId, toId, 1000);

        assertEquals(fromId, result.fromAccountId());
        assertEquals(toId, result.toAccountId());
        assertEquals(1000, result.amount());
        assertEquals("EUR", result.currency());
        assertEquals(4000, result.fromBalance());
        assertEquals(4000, result.toBalance());
        assertEquals(4000, from.getBalance());
        assertEquals(4000, to.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void transferRejectsInsufficientFunds() {
        Account from = account(fromId, AccountStatus.ACTIVE, "EUR", 100);
        Account to = account(toId, AccountStatus.ACTIVE, "EUR", 3000);
        when(accountRepository.findById(fromId)).thenReturn(Optional.of(from));
        when(accountRepository.findById(toId)).thenReturn(Optional.of(to));

        assertThrows(InsufficientFundsException.class,
                () -> accountService.transfer(fromId, toId, 500));

        assertEquals(100, from.getBalance());
        assertEquals(3000, to.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void transferRejectsBlockedAccount() {
        Account from = account(fromId, AccountStatus.ACTIVE, "EUR", 5000);
        Account to = account(toId, AccountStatus.BLOCKED, "EUR", 1500);
        when(accountRepository.findById(fromId)).thenReturn(Optional.of(from));
        when(accountRepository.findById(toId)).thenReturn(Optional.of(to));

        assertThrows(InvalidOperationException.class,
                () -> accountService.transfer(fromId, toId, 100));

        assertEquals(5000, from.getBalance());
        assertEquals(1500, to.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
    }

    private Account account(UUID id, AccountStatus status, String currency, double balance) {
        return Account.builder()
                .id(id)
                .customerReference("CUST-" + id)
                .status(status)
                .currency(currency)
                .balance(balance)
                .build();
    }
}
