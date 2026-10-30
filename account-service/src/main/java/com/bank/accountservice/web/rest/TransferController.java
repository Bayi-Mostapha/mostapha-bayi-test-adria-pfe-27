package com.bank.accountservice.web.rest;

import com.bank.accountservice.service.AccountService;
import com.bank.accountservice.service.TransferResult;
import com.bank.accountservice.web.rest.dto.TransferRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/transfers: moves an amount between two accounts in one transaction.
 */
@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final AccountService accountService;

    public TransferController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<TransferResult> transfer(@Valid @RequestBody TransferRequest request) {
        TransferResult result = accountService.transfer(
                request.fromAccountId(),
                request.toAccountId(),
                request.amount());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
