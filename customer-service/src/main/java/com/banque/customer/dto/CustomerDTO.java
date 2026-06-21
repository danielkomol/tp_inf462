package com.banque.customer.dto;

import com.banque.customer.model.Customer;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerDTO {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateCustomerRequest {
        @NotBlank private String userId;
        @NotBlank private String nom;
        @NotBlank private String prenom;
        @Email @NotBlank private String email;
        @NotBlank private String telephone;
        private LocalDate dateNaissance;
        private String lieuNaissance;
        private Customer.Sexe sexe;
        private String adresse;
        private String ville;
        private String numeroCNI;
        private Customer.TypeIdentite typeIdentite;
        private String profession;
        private Double revenuMensuel;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateCustomerRequest {
        private String adresse;
        private String ville;
        private String profession;
        private Double revenuMensuel;
        private String telephone;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CustomerResponse {
        private Long id;
        private String userId;
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
        private LocalDate dateNaissance;
        private String ville;
        private String numeroCNI;
        private String profession;
        private Double revenuMensuel;
        private Customer.StatutVerification statutVerification;
        private Integer scoreCredit;
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
