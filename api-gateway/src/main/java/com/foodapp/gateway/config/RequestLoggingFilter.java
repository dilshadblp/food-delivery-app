package com.foodapp.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String rid = UUID.randomUUID().toString().substring(0,8);
        ServerHttpRequest req = exchange.getRequest();
        log.info("[{}] {} {}", rid, req.getMethod(), req.getURI());
        return chain.filter(exchange).doOnSuccess(v -> {
            int status = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value()
                    : 200;
            log.info("[{}] -> {}", rid, status);
        });
    }

    @Override
    public int getOrder() {
        return -1; // run early
    }
}
