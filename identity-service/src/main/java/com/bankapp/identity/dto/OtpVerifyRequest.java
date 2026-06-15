package com.bankapp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerifyRequest {

    @NotBlank
    private String mfaToken;   // token temporaire reçu après login

    @NotBlank
    private String code;       // code OTP (6 chiffres)
}
