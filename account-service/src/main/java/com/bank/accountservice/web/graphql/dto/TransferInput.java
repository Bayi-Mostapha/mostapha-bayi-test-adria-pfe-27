package com.bank.accountservice.web.graphql.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GraphQL input object of the transfer mutation. The ids arrive as GraphQL ID, which
 * binds to String and is parsed to UUID by the controller.
 */
@Getter
@Setter
@NoArgsConstructor
public class TransferInput {

    private String fromAccountId;
    private String toAccountId;
    private double amount;
}
