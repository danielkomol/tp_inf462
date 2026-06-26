package com.banque.identity.dto;

import com.banque.identity.model.User;
import jakarta.validation.constraints.*;
import lombok.*;

public class AuthDTO {

    /**
     * INSCRIPTION — Données reçues du client
     */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RegisterRequest {
        @NotBlank(message = "Le nom est obligatoire")
        private String nom;

        @NotBlank(message = "Le prénom est obligatoire")
        private String prenom;

        @Email(message = "Email invalide")
        @NotBlank(message = "L'email est obligatoire")
        private String email;

        @NotBlank(message = "Le téléphone est obligatoire")
        private String telephone;

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit avoir au moins 8 caractères")
        private String motDePasse;

        private User.Role role;
    }

    /**
     * CONNEXION — Email + mot de passe
     */
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @Email @NotBlank
        private String email;

        @NotBlank
        private String motDePasse;
    }

    /**
     * RÉPONSE APRÈS CONNEXION — Contient le token JWT
     */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private Long expiresIn;
        private UserInfo user;
    }

    /**
     * INFOS UTILISATEUR renvoyées au client
     */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UserInfo {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
        private User.Role role;
        private boolean actif;
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
