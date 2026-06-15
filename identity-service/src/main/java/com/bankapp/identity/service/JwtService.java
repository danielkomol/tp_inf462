package com.bankapp.identity.service;

import com.bankapp.identity.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HexFormat;
import java.util.Map;

/**
 * Émission et validation des JWT (HS256).
 * La clé publique est exposée via /api/auth/.well-known/jwks.json
 * pour être consommée par la gateway.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtService(
            @Value("${jwt.secret}") String hexSecret,
            @Value("${jwt.access-token-expiration}") long accessExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshExpiration) {
        this.signingKey = Keys.hmacShaKeyFor(HexFormat.of().parseHex(hexSecret));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId())
                .claims(Map.of(
                        "email", user.getEmail(),
                        "role", user.getRole().name()
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(signingKey)
                .compact();
    }

    /** Token temporaire MFA (durée courte : 5 minutes). */
    public String generateMfaToken(String userId) {
        return Jwts.builder()
                .subject(userId)
                .claim("mfa_pending", true)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 300_000))
                .signWith(signingKey)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            return !extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public long getAccessExpirationSeconds() {
        return accessExpiration / 1000;
    }
}
