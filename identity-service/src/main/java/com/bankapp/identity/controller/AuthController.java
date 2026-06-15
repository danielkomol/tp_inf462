package com.bankapp.identity.controller;

import com.bankapp.identity.dto.*;
import com.bankapp.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Inscription, connexion, gestion des tokens et MFA")
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "Inscription",
        description = "Crée un nouvel utilisateur avec un rôle (CLIENT, ADMIN, OPERATEUR, AGENT).",
        responses = {
            @ApiResponse(responseCode = "201", description = "Utilisateur créé, tokens retournés"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Email déjà utilisé")
        }
    )
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @Operation(
        summary = "Connexion",
        description = "Login par email/password. Si MFA activé, retourne `mfaRequired: true` et un `mfaToken` temporaire à utiliser sur `/verify-otp`.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(value = """
                {
                  "email": "test@bank.com",
                  "password": "motdepasse123"
                }
            """))
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Tokens retournés ou challenge MFA"),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides")
        }
    )
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @Operation(
        summary = "Vérification OTP / TOTP",
        description = "Soumet le code reçu par SMS (OTP) ou généré par l'app (TOTP) après un login avec MFA. Utilise le `mfaToken` reçu à l'étape login.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(value = """
                {
                  "mfaToken": "eyJhbGci...",
                  "code": "482931"
                }
            """))
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Code valide, access + refresh tokens retournés"),
            @ApiResponse(responseCode = "401", description = "Code invalide ou expiré")
        }
    )
    @PostMapping("/verify-otp")
    public AuthResponse verifyOtp(@Valid @RequestBody OtpVerifyRequest req) {
        return authService.verifyOtp(req);
    }

    @Operation(
        summary = "Renouvellement du token",
        description = "Génère un nouvel access token à partir d'un refresh token valide. L'ancien refresh token est révoqué.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Nouveau access token retourné"),
            @ApiResponse(responseCode = "401", description = "Refresh token expiré ou révoqué")
        }
    )
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req);
    }

    @Operation(
        summary = "Déconnexion",
        description = "Révoque tous les refresh tokens de l'utilisateur connecté.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Déconnexion réussie"),
            @ApiResponse(responseCode = "401", description = "Token manquant ou invalide")
        }
    )
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal Jwt jwt) {
        authService.logout(jwt.getSubject());
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }

    @Operation(
        summary = "Initialiser la 2FA (TOTP)",
        description = "Génère une clé secrète TOTP et une URL QR code à scanner avec Google Authenticator ou Authy. Nécessite un access token valide.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Clé secrète et URL QR retournées"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
        }
    )
    @PostMapping("/2fa/setup")
    public TotpSetupResponse setupTotp(@AuthenticationPrincipal Jwt jwt) {
        return authService.setupTotp(jwt.getSubject());
    }

    @Operation(
        summary = "Confirmer l'activation 2FA",
        description = "Valide le premier code TOTP généré par l'app pour activer définitivement la 2FA sur le compte.",
        security = @SecurityRequirement(name = "bearerAuth"),
        parameters = @Parameter(name = "code", description = "Code à 6 chiffres généré par l'app TOTP", example = "482931", required = true),
        responses = {
            @ApiResponse(responseCode = "200", description = "2FA activée"),
            @ApiResponse(responseCode = "401", description = "Code TOTP invalide")
        }
    )
    @PostMapping("/2fa/confirm")
    public ResponseEntity<Map<String, String>> confirmTotp(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam int code) {
        authService.confirmTotp(jwt.getSubject(), code);
        return ResponseEntity.ok(Map.of("message", "2FA activée avec succès"));
    }

    @Operation(
        summary = "JWKS — clés publiques",
        description = "Endpoint consommé par l'API Gateway pour valider les tokens JWT émis par ce service.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Liste des clés JWK")
        }
    )
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> jwks() {
        return ResponseEntity.ok(Map.of("keys", List.of()));
    }
}
