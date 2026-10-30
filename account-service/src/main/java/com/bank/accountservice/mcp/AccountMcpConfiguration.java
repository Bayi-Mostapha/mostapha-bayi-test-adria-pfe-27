package com.bank.accountservice.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publishes the @Tool methods of AccountMcpTools. The MCP server auto-configuration picks
 * up every ToolCallbackProvider bean and registers its callbacks as MCP tools.
 */
@Configuration
public class AccountMcpConfiguration {

    @Bean
    public ToolCallbackProvider accountToolCallbackProvider(AccountMcpTools accountMcpTools) {
        return MethodToolCallbackProvider.builder().toolObjects(accountMcpTools).build();
    }
}
