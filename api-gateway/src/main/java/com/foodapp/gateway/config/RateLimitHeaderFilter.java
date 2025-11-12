package com.foodapp.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RateLimitHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        // Placeholder: just set some headers; swap with RedisRateLimiter later.
        exchange.getResponse().getHeaders().add("X-RateLimit-Policy", "placeholder");
        exchange.getResponse().getHeaders().add(HttpHeaders.VARY, "Authorization");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0; // after logging
    }
}
