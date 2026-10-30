package com.bank.accountservice.web.rest;

import com.bank.accountservice.domain.Account;
import com.bank.accountservice.service.AccountService;
import com.bank.accountservice.web.rest.dto.AccountCreateRequest;
import com.bank.accountservice.web.rest.dto.AccountResponse;
import com.bank.accountservice.web.rest.dto.AmountRequest;
import com.bank.accountservice.web.rest.dto.BalanceResponse;
import com.bank.accountservice.web.rest.dto.StatusUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST surface over AccountService. Every rule is applied by the service, this class only
 * maps HTTP to service calls and service results to JSON.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountCreateRequest request) {
        Account account = accountService.create(
                request.customerReference(),
                request.currency(),
                request.balance(),
                request.status());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(account));
    }

    @GetMapping
    public List<AccountResponse> findAll() {
        return accountService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public AccountResponse findById(@PathVariable UUID id) {
        return toResponse(accountService.findById(id));
    }

    @GetMapping("/{id}/balance")
    public BalanceResponse getBalance(@PathVariable UUID id) {
        return new BalanceResponse(accountService.getBalance(id));
    }

    @PatchMapping("/{id}/status")
    public AccountResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest request) {
        return toResponse(accountService.updateStatus(id, request.status()));
    }

    @PostMapping("/{id}/debit")
    public AccountResponse debit(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return toResponse(accountService.debit(id, request.amount()));
    }

    @PostMapping("/{id}/credit")
    public AccountResponse credit(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return toResponse(accountService.credit(id, request.amount()));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getCustomerReference(),
                account.getStatus(),
                account.getCurrency(),
                account.getBalance());
    }
}
