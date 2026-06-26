package com.banque.identity.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * SERVICE JWT — Génère et valide les tokens
 *
 * Un token JWT ressemble à ceci :
 * eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGVtYWlsLmNvbSJ9.signature
 *    |___ HEADER ___|   |_______ PAYLOAD ________|  |_ SIGNATURE _|
 *
 * HEADER  = algorithme utilisé
 * PAYLOAD = données (email, rôle, expiration...)
 * SIGNATURE = garantit que le token n'a pas été modifié
 */
@Component
@Slf4j
public class JwtService {

    // Clé secrète lue depuis application.properties
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * EXTRAIRE L'EMAIL depuis un token
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * GÉNÉRER UN ACCESS TOKEN pour un utilisateur
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // On ajoute le rôle dans le token
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
        return buildToken(claims, userDetails, jwtExpiration);
    }

    /**
     * GÉNÉRER UN REFRESH TOKEN (longue durée)
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    /**
     * VÉRIFIER SI LE TOKEN EST VALIDE
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ============================================
    // MÉTHODES PRIVÉES
    // ============================================

    private String buildToken(Map<String, Object> claims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername()) // = email
                .setIssuedAt(new Date())               // date de création
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
