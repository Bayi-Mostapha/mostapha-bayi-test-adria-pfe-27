package com.bank.accountservice.web.graphql.dto;

import com.bank.accountservice.domain.AccountStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GraphQL input object of the createAccount mutation. A plain JavaBean so that Spring
 * GraphQL can bind the input object onto it.
 */
@Getter
@Setter
@NoArgsConstructor
public class AccountCreateInput {

    private String customerReference;
    private String currency;
    private double balance;
    private AccountStatus status;
}
