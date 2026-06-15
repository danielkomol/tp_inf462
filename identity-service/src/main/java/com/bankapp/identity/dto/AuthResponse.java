package com.bankapp.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;        // secondes
    private String userId;
    private String role;

    // Si MFA requis, on renvoie un challenge au lieu des tokens
    private boolean mfaRequired;
    private String mfaToken;       // token temporaire pour valider le MFA
}
