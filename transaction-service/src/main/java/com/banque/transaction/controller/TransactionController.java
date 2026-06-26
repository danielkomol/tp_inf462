package com.banque.transaction.controller;

import com.banque.transaction.dto.TransactionDTO.*;
import com.banque.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * CONTRÔLEUR TRANSACTIONS — Endpoints REST
 *
 * POST /api/v1/transactions/depot          → Déposer de l'argent
 * POST /api/v1/transactions/retrait        → Retirer de l'argent
 * POST /api/v1/transactions/transfert/intra → Transfert même opérateur
 * POST /api/v1/transactions/transfert/inter → Transfert inter-opérateur
 * GET  /api/v1/transactions/{reference}    → Consulter une transaction
 * GET  /api/v1/transactions/client/{id}    → Historique d'un client
 * GET  /api/v1/transactions/compte/{num}   → Historique d'un compte
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "API de gestion des opérations financières")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/depot")
    @Operation(summary = "Dépôt d'argent")
    public ResponseEntity<ApiResponse<TransactionResponse>> depot(
            @Valid @RequestBody DepotRequest request) {
        TransactionResponse response = transactionService.depot(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dépôt effectué avec succès", response));
    }

    @PostMapping("/retrait")
    @Operation(summary = "Retrait d'argent")
    public ResponseEntity<ApiResponse<TransactionResponse>> retrait(
            @Valid @RequestBody RetraitRequest request) {
        TransactionResponse response = transactionService.retrait(request);
        return ResponseEntity.ok(ApiResponse.success("Retrait effectué avec succès", response));
    }

    @PostMapping("/transfert/intra")
    @Operation(summary = "Transfert intra-opérateur")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfertIntra(
            @Valid @RequestBody TransfertRequest request) {
        TransactionResponse response = transactionService.transfertIntra(request);
        return ResponseEntity.ok(ApiResponse.success("Transfert effectué avec succès", response));
    }

    @PostMapping("/transfert/inter")
    @Operation(summary = "Transfert inter-opérateur")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfertInter(
            @Valid @RequestBody TransfertRequest request) {
        TransactionResponse response = transactionService.transfertInter(request);
        return ResponseEntity.ok(ApiResponse.success("Transfert inter-opérateur initié", response));
    }

    @GetMapping("/{reference}")
    @Operation(summary = "Consulter une transaction par référence")
    public ResponseEntity<ApiResponse<TransactionResponse>> getByReference(
            @PathVariable String reference) {
        TransactionResponse response = transactionService.getByReference(reference);
        return ResponseEntity.ok(ApiResponse.success("Transaction trouvée", response));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Historique des transactions d'un client")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getHistoriqueClient(
            @PathVariable String clientId) {
        List<TransactionResponse> transactions = transactionService.getHistoriqueClient(clientId);
        return ResponseEntity.ok(ApiResponse.success(
                transactions.size() + " transaction(s) trouvée(s)", transactions));
    }

    @GetMapping("/compte/{numeroCompte}")
    @Operation(summary = "Historique des transactions d'un compte")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getHistoriqueCompte(
            @PathVariable String numeroCompte) {
        List<TransactionResponse> transactions = transactionService.getHistoriqueCompte(numeroCompte);
        return ResponseEntity.ok(ApiResponse.success(
                transactions.size() + " transaction(s) trouvée(s)", transactions));
    }
}
