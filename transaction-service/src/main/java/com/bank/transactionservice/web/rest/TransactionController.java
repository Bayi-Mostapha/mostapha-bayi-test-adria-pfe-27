package com.bank.transactionservice.web.rest;

import com.bank.transactionservice.domain.Transaction;
import com.bank.transactionservice.service.TransactionService;
import com.bank.transactionservice.web.rest.dto.TransactionCreateRequest;
import com.bank.transactionservice.web.rest.dto.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST surface over TransactionService: it only maps HTTP to service calls and never
 * applies a rule itself.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionCreateRequest request) {
        Transaction transaction = transactionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(transaction));
    }

    @GetMapping
    public List<TransactionResponse> findAll() {
        return transactionService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public TransactionResponse findById(@PathVariable UUID id) {
        return toResponse(transactionService.findById(id));
    }

    @GetMapping("/account/{accountId}")
    public List<TransactionResponse> findByAccountId(@PathVariable UUID accountId) {
        return transactionService.findByAccountId(accountId).stream().map(this::toResponse).toList();
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getMerchantCategory(),
                transaction.getCountry(),
                transaction.getCreatedAt());
    }
}
