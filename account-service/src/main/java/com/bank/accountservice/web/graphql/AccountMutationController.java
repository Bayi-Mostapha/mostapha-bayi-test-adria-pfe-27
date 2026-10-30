package com.bank.accountservice.web.graphql;

import com.bank.accountservice.domain.Account;
import com.bank.accountservice.domain.AccountStatus;
import com.bank.accountservice.service.AccountService;
import com.bank.accountservice.service.TransferResult;
import com.bank.accountservice.web.graphql.dto.AccountCreateInput;
import com.bank.accountservice.web.graphql.dto.TransferInput;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * GraphQL mutations of the account-service. Validation and business rules are applied by
 * AccountService, this class only binds the GraphQL arguments to service calls.
 */
@Controller
public class AccountMutationController {

    private final AccountService accountService;

    public AccountMutationController(AccountService accountService) {
        this.accountService = accountService;
    }

    @MutationMapping
    public Account createAccount(@Argument AccountCreateInput input) {
        return accountService.create(
                input.getCustomerReference(),
                input.getCurrency(),
                input.getBalance(),
                input.getStatus());
    }

    @MutationMapping
    public Account updateAccountStatus(@Argument String id, @Argument AccountStatus status) {
        return accountService.updateStatus(UUID.fromString(id), status);
    }

    @MutationMapping
    public TransferResult transfer(@Argument TransferInput input) {
        return accountService.transfer(
                UUID.fromString(input.getFromAccountId()),
                UUID.fromString(input.getToAccountId()),
                input.getAmount());
    }
}
