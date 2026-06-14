package com.banque.account.dto;

import com.banque.account.model.Account;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTOs = Data Transfer Objects
 *
 * Ce sont des classes simples utilisées pour :
 *   - Recevoir les données envoyées par le client (Request)
 *   - Renvoyer les données au client (Response)
 *
 * On ne renvoie JAMAIS l'entité Account directement car :
 *   - Elle peut contenir des champs sensibles
 *   - Le client n'a pas besoin de tout voir
 *   - On veut contrôler exactement ce qu'on expose
 */
public class AccountDTO {

    /**
     * DTO DE CRÉATION DE COMPTE
     * Données reçues quand un client veut ouvrir un compte
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateAccountRequest {

        // @NotBlank = ne peut pas être null ou vide
        @NotBlank(message = "L'ID du client est obligatoire")
        private String customerId;

        // @NotNull = ne peut pas être null
        @NotNull(message = "Le type de compte est obligatoire")
        private Account.AccountType accountType;

        @NotBlank(message = "L'ID de l'opérateur est obligatoire")
        private String operatorId;

        // @DecimalMin = valeur minimum autorisée
        @DecimalMin(value = "0.0", message = "Le solde initial ne peut pas être négatif")
        private BigDecimal initialBalance;

        private String currency;
    }

    /**
     * DTO DE RÉPONSE - COMPTE
     * Données renvoyées au client après une opération
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountResponse {
        private Long id;
        private String accountNumber;
        private String customerId;
        private Account.AccountType accountType;
        private BigDecimal balance;
        private String currency;
        private Account.AccountStatus status;
        private String operatorId;
        private BigDecimal transactionLimit;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * DTO POUR CRÉDITER UN COMPTE (déposer de l'argent)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreditRequest {

        @NotNull(message = "Le montant est obligatoire")
        @DecimalMin(value = "1.0", message = "Le montant minimum est 1 XAF")
        private BigDecimal amount;

        @NotBlank(message = "La description est obligatoire")
        private String description;
    }

    /**
     * DTO POUR DÉBITER UN COMPTE (retirer de l'argent)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DebitRequest {

        @NotNull(message = "Le montant est obligatoire")
        @DecimalMin(value = "1.0", message = "Le montant minimum est 1 XAF")
        private BigDecimal amount;

        @NotBlank(message = "La description est obligatoire")
        private String description;
    }

    /**
     * DTO DE RÉPONSE GÉNÉRIQUE
     * Utilisé pour renvoyer un message simple (succès ou erreur)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        // Méthodes statiques utilitaires
        public static <T> ApiResponse<T> success(String message, T data) {
            return new ApiResponse<>(true, message, data);
        }

        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, message, null);
        }
    }
}
