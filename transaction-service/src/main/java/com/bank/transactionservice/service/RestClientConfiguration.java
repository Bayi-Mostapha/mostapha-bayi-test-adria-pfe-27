package com.bank.transactionservice.service;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Spring Boot does not expose a RestClient.Builder by itself in a form the load balancer
 * accepts, so the builder used to reach account-service is declared here. The
 * LoadBalancerRestClientBuilderBeanPostProcessor of spring-cloud-commons then resolves
 * http://account-service to a real instance registered in Eureka.
 */
@Configuration
public class RestClientConfiguration {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }
}
