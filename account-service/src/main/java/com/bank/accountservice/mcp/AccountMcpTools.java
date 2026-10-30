package com.bank.accountservice.mcp;

import com.bank.accountservice.domain.Account;
import com.bank.accountservice.domain.AccountStatus;
import com.bank.accountservice.service.AccountService;
import com.bank.accountservice.service.TransferResult;
import com.bank.accountservice.web.rest.dto.AccountResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * MCP tools exposed to the chatbot. Every method delegates to AccountService and contains
 * no rule of its own: the same validation runs for REST, GraphQL and MCP callers.
 * The parameter names of the schema come from the Java method names thanks to the
 * -parameters compiler flag; @ToolParam only carries the description and the optionality.
 */
@Component
public class AccountMcpTools {

    private final AccountService accountService;

    public AccountMcpTools(AccountService accountService) {
        this.accountService = accountService;
    }

    @Tool(name = "listAccounts",
            description = "Lists every bank account with its id, customer reference, status, currency and balance.")
    public List<AccountResponse> listAccounts() {
        return accountService.findAll().stream().map(this::toDto).toList();
    }

    @Tool(name = "getAccount",
            description = "Returns one account identified by its UUID.")
    public AccountResponse getAccount(
            @ToolParam(description = "UUID of the account") String id) {
        return toDto(accountService.findById(UUID.fromString(id)));
    }

    @Tool(name = "getBalance",
            description = "Returns the current balance of an account identified by its UUID.")
    public double getBalance(
            @ToolParam(description = "UUID of the account") String id) {
        return accountService.getBalance(UUID.fromString(id));
    }

    @Tool(name = "createAccount",
            description = "Creates an account with an opening balance. The status is ACTIVE when omitted.")
    public AccountResponse createAccount(
            @ToolParam(description = "Customer identifier, for example CUST-004") String customerReference,
            @ToolParam(description = "ISO currency code, for example EUR") String currency,
            @ToolParam(description = "Opening balance, zero or more") double balance,
            @ToolParam(description = "ACTIVE or BLOCKED, optional", required = false) String status) {
        AccountStatus parsed = (status == null || status.isBlank())
                ? null
                : AccountStatus.valueOf(status.trim().toUpperCase());
        return toDto(accountService.create(customerReference, currency, balance, parsed));
    }

    @Tool(name = "updateAccountStatus",
            description = "Blocks or unblocks an account identified by its UUID.")
    public AccountResponse updateAccountStatus(
            @ToolParam(description = "UUID of the account") String id,
            @ToolParam(description = "ACTIVE or BLOCKED") String status) {
        return toDto(accountService.updateStatus(UUID.fromString(id),
                AccountStatus.valueOf(status.trim().toUpperCase())));
    }

    @Tool(name = "transferFunds",
            description = "Moves an amount from one account to another. Both accounts must be ACTIVE, "
                    + "in the same currency and the sender must hold enough funds.")
    public TransferResult transferFunds(
            @ToolParam(description = "UUID of the account to debit") String fromAccountId,
            @ToolParam(description = "UUID of the account to credit") String toAccountId,
            @ToolParam(description = "Amount to move, strictly greater than zero") double amount) {
        return accountService.transfer(UUID.fromString(fromAccountId), UUID.fromString(toAccountId), amount);
    }

    private AccountResponse toDto(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getCustomerReference(),
                account.getStatus(),
                account.getCurrency(),
                account.getBalance());
    }
}
