package com.banque.account.controller;

import com.banque.account.dto.AccountDTO.*;
import com.banque.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTRÔLEUR DES COMPTES — ENDPOINTS REST
 *
 * @RestController = @Controller + @ResponseBody
 *   Indique que toutes les méthodes retournent du JSON automatiquement
 *
 * @RequestMapping = préfixe commun à tous les endpoints de ce contrôleur
 *   Tous les endpoints commencent par /api/v1/accounts
 *
 * @Tag = annotation Swagger pour documenter le groupe d'endpoints
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestion des Comptes", description = "API de gestion des comptes bancaires")
public class AccountController {

    private final AccountService accountService;

    /**
     * POST /api/v1/accounts
     * Créer un nouveau compte bancaire
     *
     * @Valid = déclenche la validation des contraintes du DTO (@NotBlank, @NotNull...)
     * ResponseEntity = permet de contrôler le statut HTTP de la réponse
     * HttpStatus.CREATED = 201 (la ressource a été créée)
     */
    @PostMapping
    @Operation(summary = "Créer un compte", description = "Ouvre un nouveau compte bancaire pour un client")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {

        log.info("Requête de création de compte reçue pour : {}", request.getCustomerId());
        AccountResponse account = accountService.createAccount(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Compte créé avec succès", account));
    }

    /**
     * GET /api/v1/accounts/{id}
     * Consulter un compte par son ID
     *
     * @PathVariable = récupère la valeur dans l'URL ({id})
     */
    @GetMapping
    @Operation(summary = "Lister tous les comptes actifs de la plateforme")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts() {
        List<AccountResponse> accounts = accountService.getAllActive();
        return ResponseEntity.ok(ApiResponse.success(accounts.size() + " compte(s)", accounts));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un compte par ID")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(
            @Parameter(description = "ID du compte") @PathVariable Long id) {

        AccountResponse account = accountService.getAccountById(id);
        return ResponseEntity.ok(ApiResponse.success("Compte récupéré", account));
    }

    /**
     * GET /api/v1/accounts/number/{accountNumber}
     * Consulter un compte par son numéro
     */
    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Consulter un compte par numéro")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(
            @PathVariable String accountNumber) {

        AccountResponse account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Compte récupéré", account));
    }

    /**
     * GET /api/v1/accounts/customer/{customerId}
     * Lister tous les comptes d'un client
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Lister les comptes d'un client")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByCustomer(
            @PathVariable String customerId) {

        List<AccountResponse> accounts = accountService.getAccountsByCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success(
                accounts.size() + " compte(s) trouvé(s)", accounts));
    }

    /**
     * POST /api/v1/accounts/{id}/credit
     * Créditer un compte (déposer de l'argent)
     */
    @PostMapping("/{id}/credit")
    @Operation(summary = "Créditer un compte", description = "Déposer de l'argent sur un compte")
    public ResponseEntity<ApiResponse<AccountResponse>> creditAccount(
            @PathVariable Long id,
            @Valid @RequestBody CreditRequest request) {

        log.info("Crédit du compte {} : {} XAF", id, request.getAmount());
        AccountResponse account = accountService.creditAccount(id, request);
        return ResponseEntity.ok(ApiResponse.success("Compte crédité avec succès", account));
    }

    /**
     * POST /api/v1/accounts/{id}/debit
     * Débiter un compte (retirer de l'argent)
     */
    @PostMapping("/{id}/debit")
    @Operation(summary = "Débiter un compte", description = "Retirer de l'argent d'un compte")
    public ResponseEntity<ApiResponse<AccountResponse>> debitAccount(
            @PathVariable Long id,
            @Valid @RequestBody DebitRequest request) {

        log.info("Débit du compte {} : {} XAF", id, request.getAmount());
        AccountResponse account = accountService.debitAccount(id, request);
        return ResponseEntity.ok(ApiResponse.success("Débit effectué avec succès", account));
    }

    /**
     * PUT /api/v1/accounts/{id}/block
     * Bloquer un compte
     */
    @PutMapping("/{id}/block")
    @Operation(summary = "Bloquer un compte")
    public ResponseEntity<ApiResponse<AccountResponse>> blockAccount(@PathVariable Long id) {
        AccountResponse account = accountService.blockAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Compte bloqué", account));
    }

    /**
     * PUT /api/v1/accounts/{id}/close
     * Fermer un compte
     */
    @PutMapping("/{id}/close")
    @Operation(summary = "Fermer un compte")
    public ResponseEntity<ApiResponse<AccountResponse>> closeAccount(@PathVariable Long id) {
        AccountResponse account = accountService.closeAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Compte fermé", account));
    }
}
