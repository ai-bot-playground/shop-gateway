package com.shop.gateway;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class UserIdGlobalFilter implements GlobalFilter, Ordered {

    public static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path == null || !path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        String userId = exchange.getRequest().getHeaders().getFirst(USER_ID_HEADER);
        if (userId == null || userId.isBlank()) {
            userId = UUID.randomUUID().toString();
        }

        String finalUserId = userId;
        var mutatedRequest = exchange.getRequest().mutate()
                .header(USER_ID_HEADER, finalUserId)
                .build();

        var mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
