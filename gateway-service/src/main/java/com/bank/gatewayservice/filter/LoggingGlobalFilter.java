package com.bank.gatewayservice.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Logs every request that passes through the gateway, with its response status and duration.
 * The completion is logged with doFinally so that failed calls (503 when a service has no
 * instance, timeouts, errors) are logged too, not only successful ones.
 */
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpMethod method = exchange.getRequest().getMethod();
        String path = exchange.getRequest().getURI().getPath();
        long start = System.currentTimeMillis();
        log.info("Gateway request started: {} {}", method, path);
        return chain.filter(exchange).doFinally(signal -> log.info(
                "Gateway request finished: {} {} -> {} in {} ms ({})",
                method,
                path,
                exchange.getResponse().getStatusCode(),
                System.currentTimeMillis() - start,
                signal));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
