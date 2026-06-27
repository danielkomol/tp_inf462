package com.banque.loan.dto;

import com.banque.loan.model.Echeance;
import com.banque.loan.model.LoanRequest;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class LoanDTO {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LoanSubmitRequest {
        @NotBlank(message = "L'ID client est obligatoire")
        private String clientId;

        @NotBlank(message = "L'ID opérateur est obligatoire")
        private String operateurId;

        @NotNull(message = "Le montant est obligatoire")
        @DecimalMin(value = "10000.0", message = "Montant minimum : 10 000 XAF")
        @DecimalMax(value = "50000000.0", message = "Montant maximum : 50 000 000 XAF")
        private BigDecimal montantDemande;

        @NotNull(message = "La durée est obligatoire")
        @Min(value = 1, message = "Durée minimum : 1 mois")
        @Max(value = 120, message = "Durée maximum : 120 mois")
        private Integer duree;

        @NotBlank(message = "Le motif est obligatoire")
        private String motif;

        @NotBlank(message = "Le compte de versement est obligatoire")
        private String compteVersement;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ValidationRequest {
        @NotNull
        private Boolean approuve;
        private BigDecimal montantAccorde;
        private BigDecimal tauxInteret;
        private String motifRejet;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class RemboursementRequest {
        @NotNull
        private Long echeanceId;
        @NotBlank
        private String compteDebit;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LoanResponse {
        private Long id;
        private String reference;
        private String clientId;
        private String operateurId;
        private BigDecimal montantDemande;
        private BigDecimal montantAccorde;
        private Integer duree;
        private BigDecimal tauxInteret;
        private String motif;
        private LoanRequest.StatutDemande statut;
        private String motifRejet;
        private String compteVersement;
        private List<EcheanceResponse> echeances;
        private LocalDateTime createdAt;
        private LocalDateTime validatedAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EcheanceResponse {
        private Long id;
        private Integer numero;
        private LocalDate dateEcheance;
        private BigDecimal montantTotal;
        private BigDecimal partCapital;
        private BigDecimal partInteret;
        private BigDecimal capitalRestant;
        private BigDecimal penalite;
        private Echeance.StatutEcheance statut;
        private LocalDateTime datePaiement;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
    }
}
