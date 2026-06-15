package com.bankapp.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtre global qui vérifie la présence du header Authorization
 * avant de transmettre la requête au service cible.
 * La validation réelle du token est faite par Spring Security (SecurityConfig).
 * Ce filtre propage aussi le token aux services downstream.
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    // Endpoints qui ne nécessitent pas de token
    private static final java.util.List<String> PUBLIC_PATHS = java.util.List.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh",
        "/actuator/health"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Laisser passer les routes publiques
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        if (isPublic) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Propager le token JWT aux services en aval
        ServerWebExchange mutatedExchange = exchange.mutate()
            .request(r -> r.header(HttpHeaders.AUTHORIZATION, authHeader))
            .build();

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return -1; // Exécuté en premier
    }
}
