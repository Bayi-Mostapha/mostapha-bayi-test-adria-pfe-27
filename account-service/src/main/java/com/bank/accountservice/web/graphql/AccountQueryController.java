package com.bank.accountservice.web.graphql;

import com.bank.accountservice.domain.Account;
import com.bank.accountservice.domain.AccountNotFoundException;
import com.bank.accountservice.service.AccountService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

/**
 * GraphQL queries of the account-service. It only calls AccountService, every rule stays
 * in the service layer, exactly like the REST controller.
 */
@Controller
public class AccountQueryController {

    private final AccountService accountService;

    public AccountQueryController(AccountService accountService) {
        this.accountService = accountService;
    }

    @QueryMapping
    public List<Account> accounts() {
        return accountService.findAll();
    }

    @QueryMapping
    public Account accountById(@Argument String id) {
        try {
            return accountService.findById(UUID.fromString(id));
        } catch (AccountNotFoundException ex) {
            return null;
        }
    }

    @QueryMapping
    public List<Account> accountsByCustomer(@Argument String customerReference) {
        try {
            return List.of(accountService.findByCustomerReference(customerReference));
        } catch (AccountNotFoundException ex) {
            return List.of();
        }
    }
}
