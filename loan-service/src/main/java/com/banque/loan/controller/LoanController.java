package com.banque.loan.controller;

import com.banque.loan.dto.LoanDTO;
import com.banque.loan.dto.LoanDTO.ApiResponse;
import com.banque.loan.dto.LoanDTO.LoanResponse;
import com.banque.loan.dto.LoanDTO.LoanSubmitRequest;
import com.banque.loan.dto.LoanDTO.ValidationRequest;
import com.banque.loan.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
            @Valid @RequestBody LoanSubmitRequest request) {
        LoanResponse response = loanService.soumettreDemande(request);
        ApiResponse<LoanResponse> res = ApiResponse.<LoanResponse>builder()
                .success(true).message("Demande soumise avec succès").data(response).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/{reference}")
    @Operation(summary = "Consulter un prêt par référence")
    public ResponseEntity<ApiResponse<LoanResponse>> getDemande(@PathVariable String reference) {
        ApiResponse<LoanResponse> res = ApiResponse.<LoanResponse>builder()
                .success(true).message("Prêt trouvé").data(loanService.getDemande(reference)).build();
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}/validate")
    @Operation(summary = "Valider ou rejeter une demande")
    public ResponseEntity<ApiResponse<LoanResponse>> valider(
            @PathVariable Long id,
            @RequestBody ValidationRequest request) {
        LoanResponse response = loanService.validerDemande(id, request);
        String msg = Boolean.TRUE.equals(request.getApprouve()) ? "Prêt approuvé" : "Prêt rejeté";
        ApiResponse<LoanResponse> res = ApiResponse.<LoanResponse>builder()
                .success(true).message(msg).data(response).build();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Lister les prêts d'un client")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getDemandesClient(
            @PathVariable String clientId) {
        List<LoanResponse> loans = loanService.getDemandesClient(clientId);
        ApiResponse<List<LoanResponse>> res = ApiResponse.<List<LoanResponse>>builder()
                .success(true).message(loans.size() + " prêt(s) trouvé(s)").data(loans).build();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/pending/{operateurId}")
    @Operation(summary = "Dossiers en attente de validation")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getDemandesEnAttente(
            @PathVariable String operateurId) {
        List<LoanResponse> loans = loanService.getDemandesEnAttente(operateurId);
        ApiResponse<List<LoanResponse>> res = ApiResponse.<List<LoanResponse>>builder()
                .success(true).message(loans.size() + " dossier(s) en attente").data(loans).build();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/all")
    @Operation(summary = "Lister tous les prêts (admin)")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getAll() {
        List<LoanResponse> loans = loanService.getAll();
        ApiResponse<List<LoanResponse>> res = ApiResponse.<List<LoanResponse>>builder()
                .success(true).message(loans.size() + " demande(s)").data(loans).build();
        return ResponseEntity.ok(res);
    }
}
