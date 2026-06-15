package com.bankapp.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TotpSetupResponse {
    private String secret;     // clé base32 à entrer dans l'app
    private String qrCodeUrl;  // URL otpauth:// pour QR code
}
