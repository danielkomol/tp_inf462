package com.banque.identity.controller;

import com.banque.identity.dto.AuthDTO.*;
import com.banque.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * CONTRÔLEUR D'AUTHENTIFICATION
 *
 * Endpoints disponibles :
 * POST /api/v1/auth/register  → Inscription
 * POST /api/v1/auth/login     → Connexion
 * GET  /api/v1/auth/me        → Profil connecté
 * PUT  /api/v1/auth/deactivate/{id} → Désactiver un compte
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentification", description = "API d'inscription, connexion et gestion des identités")
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/register
     * Inscrire un nouvel utilisateur
     */
    @PostMapping("/register")
    @Operation(summary = "Inscription", description = "Crée un nouveau compte utilisateur")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Requête d'inscription pour : {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inscription réussie", response));
    }

    /**
     * POST /api/v1/auth/login
     * Connecter un utilisateur existant
     */
    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne un token JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Requête de connexion pour : {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Connexion réussie", response));
    }

    /**
     * GET /api/v1/auth/me
     * Récupérer le profil de l'utilisateur connecté
     * @AuthenticationPrincipal = Spring injecte automatiquement l'utilisateur connecté
     */
    @GetMapping("/me")
    @Operation(summary = "Mon profil", description = "Retourne les infos de l'utilisateur connecté")
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        UserInfo user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profil récupéré", user));
    }

    /**
     * PUT /api/v1/auth/deactivate/{id}
     * Désactiver un compte utilisateur (Admin seulement)
     */
    @PutMapping("/deactivate/{id}")
    @Operation(summary = "Désactiver un compte")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        authService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("Compte désactivé", null));
    }
}
