package com.banque.transaction.dto;

import com.banque.transaction.model.Transaction;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTO {

    /**
     * DÉPÔT D'ARGENT
     */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DepotRequest {
        @NotBlank(message = "Le compte est obligatoire")
        private String compteDestinataire;

        @NotNull(message = "Le montant est obligatoire")
        @DecimalMin(value = "1.0", message = "Montant minimum : 1 XAF")
        private BigDecimal montant;

        @NotBlank(message = "La description est obligatoire")
        private String description;

        private String clientId;
        private String operateurId;
    }

    /**
     * RETRAIT D'ARGENT
     */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RetraitRequest {
        @NotBlank(message = "Le compte est obligatoire")
        private String compteSource;

        @NotNull(message = "Le montant est obligatoire")
        @DecimalMin(value = "1.0", message = "Montant minimum : 1 XAF")
        private BigDecimal montant;

        @NotBlank(message = "La description est obligatoire")
        private String description;

        private String clientId;
        private String operateurId;
    }

    /**
     * TRANSFERT (intra ou inter opérateur)
     */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TransfertRequest {
        @NotBlank(message = "Le compte source est obligatoire")
        private String compteSource;

        @NotBlank(message = "Le compte destinataire est obligatoire")
        private String compteDestinataire;

        @NotNull(message = "Le montant est obligatoire")
        @DecimalMin(value = "1.0", message = "Montant minimum : 1 XAF")
        private BigDecimal montant;

        @NotBlank(message = "La description est obligatoire")
        private String description;

        private String clientSourceId;
        private String clientDestId;
        private String operateurSourceId;
        private String operateurDestId;
    }

    /**
     * RÉPONSE TRANSACTION
     */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TransactionResponse {
        private Long id;
        private String reference;
        private Transaction.TransactionType type;
        private Transaction.TransactionStatus status;
        private BigDecimal montant;
        private BigDecimal frais;
        private String devise;
        private String compteSource;
        private String compteDestinataire;
        private String description;
        private String motifRejet;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
    }

    /**
     * RÉPONSE GÉNÉRIQUE
     */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(String message, T data) {
            return new ApiResponse<>(true, message, data);
        }
        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, message, null);
        }
    }
}
