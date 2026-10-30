package com.bank.transactionservice.service;

import com.bank.transactionservice.domain.AccountNotFoundException;
import com.bank.transactionservice.domain.AccountServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * AccountGateway over HTTP. The base url uses the Eureka service id "account-service",
 * @LoadBalanced lets Spring Cloud replace the host by a real instance chosen in Eureka.
 */
@Component
public class RestClientAccountGateway implements AccountGateway {

    private static final Logger log = LoggerFactory.getLogger(RestClientAccountGateway.class);

    private final RestClient restClient;

    public RestClientAccountGateway(@LoadBalanced RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://account-service").build();
    }

    @Override
    public AccountSnapshot getAccount(UUID accountId) {
        try {
            AccountSnapshot account = restClient.get()
                    .uri("/api/accounts/{id}", accountId)
                    .retrieve()
                    .body(AccountSnapshot.class);
            log.debug("Read account {} from account-service", accountId);
            return account;
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            throw new AccountNotFoundException(accountId);
        } catch (RestClientException ex) {
            throw new AccountServiceUnavailableException("account-service is not reachable: " + ex.getMessage());
        }
    }

    @Override
    public AccountSnapshot debit(UUID accountId, BigDecimal amount) {
        try {
            AccountSnapshot account = restClient.post()
                    .uri("/api/accounts/{id}/debit", accountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("amount", amount))
                    .retrieve()
                    .body(AccountSnapshot.class);
            log.debug("Debited {} from account {}", amount, accountId);
            return account;
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            throw new AccountNotFoundException(accountId);
        } catch (RestClientException ex) {
            throw new AccountServiceUnavailableException("account-service is not reachable: " + ex.getMessage());
        }
    }
}
