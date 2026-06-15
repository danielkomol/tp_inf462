package com.bankapp.identity.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

/**
 * Gestion du TOTP (RFC 6238) compatible Google Authenticator / Authy.
 * Utilisé pour la 2FA.
 */
@Service
public class TotpService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    /**
     * Génère une nouvelle clé secrète TOTP pour un utilisateur.
     * @return clé en base32 (à stocker en DB) et URL QR code
     */
    public GoogleAuthenticatorKey generateSecret() {
        return gAuth.createCredentials();
    }

    /**
     * Génère l'URL otpauth:// pour générer le QR code côté client.
     */
    public String getQrCodeUrl(String email, GoogleAuthenticatorKey key) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                "BankApp", email, key);
    }

    /**
     * Valide le code TOTP soumis par l'utilisateur contre la clé secrète stockée.
     */
    public boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}
