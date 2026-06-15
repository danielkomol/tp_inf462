package com.bankapp.identity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * Génération et validation des OTP (codes à usage unique envoyés par SMS/email).
 * Les codes sont stockés dans Redis avec une TTL configurée.
 */
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final String OTP_PREFIX = "otp:";
    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.expiration-seconds:300}")
    private long expirationSeconds;

    @Value("${otp.length:6}")
    private int otpLength;

    /**
     * Génère un code OTP numérique et le stocke dans Redis.
     * @param userId identifiant de l'utilisateur
     * @return le code généré (à envoyer par SMS/email)
     */
    public String generateOtp(String userId) {
        String code = generateNumericCode(otpLength);
        String key = OTP_PREFIX + userId;
        redisTemplate.opsForValue().set(key, code, Duration.ofSeconds(expirationSeconds));
        return code;
    }

    /**
     * Vérifie le code OTP soumis par l'utilisateur.
     * Le code est supprimé après une vérification réussie (usage unique).
     */
    public boolean verifyOtp(String userId, String code) {
        String key = OTP_PREFIX + userId;
        String stored = redisTemplate.opsForValue().get(key);
        if (stored != null && stored.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    private String generateNumericCode(int length) {
        int max = (int) Math.pow(10, length);
        int code = secureRandom.nextInt(max);
        return String.format("%0" + length + "d", code);
    }
}
