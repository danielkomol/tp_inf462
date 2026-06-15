package com.bankapp.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.secret}")
    private String hexSecret;

    /**
     * Décode les tokens JWT signés en HS256 par l'identity-service.
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        byte[] keyBytes = HexFormat.of().parseHex(hexSecret);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // Endpoints publics — pas besoin de token
                .pathMatchers("/api/auth/login",
                              "/api/auth/register",
                              "/api/auth/refresh",
                              "/api/auth/verify-otp",
                              "/api/auth/.well-known/jwks.json",
                              "/actuator/health").permitAll()
                // Tout le reste nécessite un JWT valide
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
            )
            .build();
    }
}
