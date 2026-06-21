package com.banque.operator.dto;

import com.banque.operator.model.Operator;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OperatorDTO {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateOperatorRequest {
        @NotBlank private String code;
        @NotBlank private String nom;
        @NotNull private Operator.TypeOperateur type;
        @Email @NotBlank private String email;
        @NotBlank private String telephone;
        private BigDecimal plafondTransaction;
        private BigDecimal plafondSolde;
        private BigDecimal tauxCommission;
        private BigDecimal tauxInteretDefaut;
        private BigDecimal plafondPret;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateRulesRequest {
        private BigDecimal plafondTransaction;
        private BigDecimal plafondSolde;
        private BigDecimal tauxCommission;
        private BigDecimal tauxInteretDefaut;
        private BigDecimal plafondPret;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OperatorResponse {
        private Long id;
        private String code;
        private String nom;
        private Operator.TypeOperateur type;
        private String email;
        private String telephone;
        private BigDecimal plafondTransaction;
        private BigDecimal plafondSolde;
        private BigDecimal tauxCommission;
        private BigDecimal tauxInteretDefaut;
        private BigDecimal plafondPret;
        private Operator.StatutOperateur statut;
        private LocalDateTime createdAt;
    }

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
