package com.banque.identity.config;

import com.banque.identity.repository.UserRepository;
import com.banque.identity.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * CONFIGURATION DE SÉCURITÉ
 *
 * Définit :
 * - Quels endpoints sont publics (pas besoin de token)
 * - Quels endpoints sont protégés (token obligatoire)
 * - Comment les mots de passe sont hashés
 * - Comment l'authentification fonctionne
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserRepository userRepository;

    /**
     * CONFIGURATION DES RÈGLES DE SÉCURITÉ HTTP
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactive CSRF (pas nécessaire pour une API REST stateless)
            .csrf(csrf -> csrf.disable())

            // Définit qui peut accéder à quoi
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics (pas besoin de token)
                .requestMatchers(
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh",
                    "/swagger-ui/**",
                    "/api-docs/**"
                ).permitAll()
                // Tous les autres endpoints nécessitent un token
                .anyRequest().authenticated()
            )

            // Pas de session (API REST = stateless)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Ajoute notre filtre JWT avant le filtre d'authentification par défaut
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CHARGE UN UTILISATEUR DEPUIS LA BASE DE DONNÉES
     * Spring Security appelle cette méthode lors de l'authentification
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + email));
    }

    /**
     * ENCODEUR DE MOT DE PASSE
     * BCrypt = algorithme sécurisé de hashage
     * Le facteur 12 = plus c'est élevé, plus c'est lent à cracker
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * GESTIONNAIRE D'AUTHENTIFICATION
     * Utilisé pour vérifier email + mot de passe lors du login
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
