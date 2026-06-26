package com.banque.loan.controller;

import com.banque.loan.dto.LoanDTO.*;
import com.banque.loan.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * CONTRÔLEUR PRÊTS — Endpoints REST
 *
 * POST /api/v1/loans                    → Soumettre une demande
 * GET  /api/v1/loans/{reference}        → Consulter un prêt
 * PUT  /api/v1/loans/{id}/validate      → Valider/Rejeter (opérateur)
 * GET  /api/v1/loans/client/{clientId}  → Prêts d'un client
 * GET  /api/v1/loans/pending/{opId}     → Dossiers en attente
 */
@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Prêts", description = "API de gestion des prêts et crédits")
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    @Operation(summary = "Soumettre une demande de prêt")
    public ResponseEntity<ApiResponse<LoanResponse>> soumettre(
            @Valid @RequestBody LoanDTO.LoanRequest request) {
        LoanResponse response = loanService.soumettreDemande(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Demande soumise avec succès", response));
    }

    @GetMapping("/{reference}")
    @Operation(summary = "Consulter un prêt par référence")
    public ResponseEntity<ApiResponse<LoanResponse>> getDemande(
            @PathVariable String reference) {
        return ResponseEntity.ok(ApiResponse.success("Prêt trouvé", loanService.getDemande(reference)));
    }

    @PutMapping("/{id}/validate")
    @Operation(summary = "Valider ou rejeter une demande (opérateur)")
    public ResponseEntity<ApiResponse<LoanResponse>> valider(
            @PathVariable Long id,
            @RequestBody ValidationRequest request) {
        LoanResponse response = loanService.validerDemande(id, request);
        return ResponseEntity.ok(ApiResponse.success(
                request.getApprouve() ? "Prêt approuvé" : "Prêt rejeté", response));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Lister les prêts d'un client")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getDemandesClient(
            @PathVariable String clientId) {
        List<LoanResponse> loans = loanService.getDemandesClient(clientId);
        return ResponseEntity.ok(ApiResponse.success(loans.size() + " prêt(s) trouvé(s)", loans));
    }

    @GetMapping("/pending/{operateurId}")
    @Operation(summary = "Dossiers en attente de validation")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getDemandesEnAttente(
            @PathVariable String operateurId) {
        List<LoanResponse> loans = loanService.getDemandesEnAttente(operateurId);
        return ResponseEntity.ok(ApiResponse.success(loans.size() + " dossier(s) en attente", loans));
    }
}
