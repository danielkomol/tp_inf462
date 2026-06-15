package com.bankapp.identity.service;

import com.bankapp.identity.dto.*;
import com.bankapp.identity.entity.RefreshToken;
import com.bankapp.identity.entity.User;
import com.bankapp.identity.exception.AuthException;
import com.bankapp.identity.repository.RefreshTokenRepository;
import com.bankapp.identity.repository.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final TotpService totpService;
    private final PasswordEncoder passwordEncoder;

    @Nullable
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiration;

    @Autowired
    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtService jwtService,
                       OtpService otpService,
                       TotpService totpService,
                       PasswordEncoder passwordEncoder,
                       @Nullable KafkaTemplate<String, Object> kafkaTemplate) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.totpService = totpService;
        this.passwordEncoder = passwordEncoder;
        this.kafkaTemplate = kafkaTemplate;
    }

    // ── Inscription ───────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AuthException("Email déjà utilisé");
        }

        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phoneNumber(req.getPhoneNumber())
                .role(req.getRole())
                .build();

        user = userRepository.save(user);
        log.info("Nouvel utilisateur enregistré : {}", user.getEmail());

        sendEvent("user.registered", Map.of(
                "userId", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        ));

        return buildFullAuthResponse(user);
    }

    // ── Login ─────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new AuthException("Identifiants invalides"));

        if (!user.isEnabled()) {
            throw new AuthException("Compte désactivé");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new AuthException("Identifiants invalides");
        }

        if (user.isTwoFactorEnabled()) {
            String mfaToken = jwtService.generateMfaToken(user.getId());
            return AuthResponse.builder()
                    .mfaRequired(true)
                    .mfaToken(mfaToken)
                    .build();
        }

        if (user.isOtpEnabled()) {
            String code = otpService.generateOtp(user.getId());
            String mfaToken = jwtService.generateMfaToken(user.getId());
            sendEvent("otp.requested", Map.of(
                    "userId", user.getId(),
                    "email", user.getEmail(),
                    "phone", user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                    "code", code
            ));
            return AuthResponse.builder()
                    .mfaRequired(true)
                    .mfaToken(mfaToken)
                    .build();
        }

        return buildFullAuthResponse(user);
    }

    // ── Vérification OTP ──────────────────────────────────────────────

    @Transactional
    public AuthResponse verifyOtp(OtpVerifyRequest req) {
        if (!jwtService.isTokenValid(req.getMfaToken())) {
            throw new AuthException("Token MFA expiré ou invalide");
        }

        String userId = jwtService.extractUserId(req.getMfaToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("Utilisateur introuvable"));

        boolean valid;
        if (user.isTwoFactorEnabled()) {
            valid = totpService.verifyCode(user.getTwoFactorSecret(), Integer.parseInt(req.getCode()));
        } else {
            valid = otpService.verifyOtp(userId, req.getCode());
        }

        if (!valid) {
            throw new AuthException("Code invalide ou expiré");
        }

        return buildFullAuthResponse(user);
    }

    // ── Refresh Token ─────────────────────────────────────────────────

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        RefreshToken stored = refreshTokenRepository.findByToken(req.getRefreshToken())
                .orElseThrow(() -> new AuthException("Refresh token invalide"));

        if (stored.isRevoked() || stored.getExpiryDate().isBefore(Instant.now())) {
            throw new AuthException("Refresh token expiré ou révoqué");
        }

        User user = stored.getUser();
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildFullAuthResponse(user);
    }

    // ── Logout ────────────────────────────────────────────────────────

    @Transactional
    public void logout(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            refreshTokenRepository.revokeAllUserTokens(user);
            log.info("Utilisateur déconnecté : {}", user.getEmail());
        });
    }

    // ── Setup 2FA (TOTP) ──────────────────────────────────────────────

    @Transactional
    public TotpSetupResponse setupTotp(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("Utilisateur introuvable"));

        GoogleAuthenticatorKey key = totpService.generateSecret();
        user.setTwoFactorSecret(key.getKey());
        userRepository.save(user);

        String qrUrl = totpService.getQrCodeUrl(user.getEmail(), key);
        return new TotpSetupResponse(key.getKey(), qrUrl);
    }

    @Transactional
    public void confirmTotp(String userId, int code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("Utilisateur introuvable"));

        if (!totpService.verifyCode(user.getTwoFactorSecret(), code)) {
            throw new AuthException("Code TOTP invalide");
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void sendEvent(String topic, Map<String, Object> payload) {
        if (kafkaTemplate == null) {
            log.warn("Kafka non disponible, événement '{}' ignoré", topic);
            return;
        }
        try {
            kafkaTemplate.send(topic, payload);
        } catch (Exception e) {
            log.warn("Erreur envoi événement Kafka '{}' : {}", topic, e.getMessage());
        }
    }

    private AuthResponse buildFullAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessExpirationSeconds())
                .userId(user.getId())
                .role(user.getRole().name())
                .mfaRequired(false)
                .build();
    }
}
