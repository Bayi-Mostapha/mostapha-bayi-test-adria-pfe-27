package com.bank.transactionservice.service;

import com.bank.transactionservice.domain.AccountNotActiveException;
import com.bank.transactionservice.domain.CurrencyMismatchException;
import com.bank.transactionservice.domain.InsufficientBalanceException;
import com.bank.transactionservice.domain.Transaction;
import com.bank.transactionservice.domain.TransactionLimitExceededException;
import com.bank.transactionservice.repository.TransactionRepository;
import com.bank.transactionservice.web.rest.dto.TransactionCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests of the business rules of TransactionService, with every collaborator mocked.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final String EUR = "EUR";

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountGateway accountGateway;

    @Mock
    private TransactionEventPublisher eventPublisher;

    @Mock
    private TransactionLimitHolder limitHolder;

    private TransactionService transactionService;

    private UUID accountId;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository, accountGateway, eventPublisher, limitHolder);
        accountId = UUID.randomUUID();
        when(limitHolder.getMaxAmount()).thenReturn(1000.0);
    }

    @Test
    void rejectsAmountOverTheLimit() {
        TransactionCreateRequest request = request(new BigDecimal("1500.00"), EUR);

        assertThrows(TransactionLimitExceededException.class, () -> transactionService.create(request));

        verifyNoInteractions(accountGateway, transactionRepository, eventPublisher);
    }

    @Test
    void rejectsBlockedAccount() {
        when(accountGateway.getAccount(accountId))
                .thenReturn(new AccountSnapshot(accountId, "CUST-001", "BLOCKED", EUR, 5000));
        TransactionCreateRequest request = request(new BigDecimal("100.00"), EUR);

        assertThrows(AccountNotActiveException.class, () -> transactionService.create(request));

        verify(transactionRepository, never()).save(any(Transaction.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void rejectsCurrencyMismatch() {
        when(accountGateway.getAccount(accountId))
                .thenReturn(new AccountSnapshot(accountId, "CUST-001", "ACTIVE", EUR, 5000));
        TransactionCreateRequest request = request(new BigDecimal("100.00"), "USD");

        assertThrows(CurrencyMismatchException.class, () -> transactionService.create(request));

        verify(transactionRepository, never()).save(any(Transaction.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void rejectsInsufficientFunds() {
        when(accountGateway.getAccount(accountId))
                .thenReturn(new AccountSnapshot(accountId, "CUST-001", "ACTIVE", EUR, 50));
        TransactionCreateRequest request = request(new BigDecimal("100.00"), EUR);

        assertThrows(InsufficientBalanceException.class, () -> transactionService.create(request));

        verify(transactionRepository, never()).save(any(Transaction.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void savesAndDebitsWhenEverythingIsValid() {
        AccountSnapshot account = new AccountSnapshot(accountId, "CUST-001", "ACTIVE", EUR, 5000);
        when(accountGateway.getAccount(accountId)).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction saved = transactionService.create(request(new BigDecimal("250.00"), EUR));

        assertEquals(0, saved.getAmount().compareTo(new BigDecimal("250.00")));
        assertEquals(accountId, saved.getAccountId());
        verify(accountGateway).debit(accountId, new BigDecimal("250.00"));
        verify(eventPublisher).publish(any(Transaction.class));
    }

    private TransactionCreateRequest request(BigDecimal amount, String currency) {
        return new TransactionCreateRequest(accountId, amount, currency, "GROCERIES", "FR");
    }
}
