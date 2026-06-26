package com.banque.identity.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * FILTRE JWT
 *
 * Ce filtre est exécuté à CHAQUE requête HTTP.
 * Il vérifie si un token JWT valide est présent dans le header.
 *
 * Fonctionnement :
 * 1. Requête arrive
 * 2. Filtre lit le header "Authorization: Bearer <token>"
 * 3. Extrait et valide le token
 * 4. Si valide → l'utilisateur est authentifié
 * 5. Si invalide → rejeté avec 401 Unauthorized
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Lire le header Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. Si pas de token → on laisse passer (sera rejeté par Spring Security si endpoint protégé)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraire le token (enlever "Bearer ")
        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            userEmail = jwtService.extractEmail(jwt);
        } catch (Exception e) {
            log.error("Token JWT invalide : {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Si email extrait et pas encore authentifié
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // 5. Vérifier la validité du token
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // 6. Créer l'objet d'authentification
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. Enregistrer dans le contexte de sécurité
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Utilisateur authentifié : {}", userEmail);
            }
        }

        filterChain.doFilter(request, response);
    }
}
