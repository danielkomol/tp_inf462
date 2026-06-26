package com.banque.identity.service;

import com.banque.identity.dto.AuthDTO.*;
import com.banque.identity.model.User;
import com.banque.identity.repository.UserRepository;
import com.banque.identity.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * SERVICE D'AUTHENTIFICATION — Logique métier
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * INSCRIPTION D'UN NOUVEL UTILISATEUR
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Inscription de : {}", request.getEmail());

        // 1. Vérifier que l'email n'existe pas déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé : " + request.getEmail());
        }

        // 2. Vérifier le téléphone
        if (userRepository.existsByTelephone(request.getTelephone())) {
            throw new RuntimeException("Ce téléphone est déjà utilisé");
        }

        // 3. Créer l'utilisateur avec mot de passe hashé
        User user = User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                // IMPORTANT : on ne stocke JAMAIS le mot de passe en clair !
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .role(request.getRole() != null ? request.getRole() : User.Role.CLIENT)
                .actif(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Utilisateur créé : {}", savedUser.getEmail());

        // 4. Publier événement Kafka
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "USER_CREATED");
        event.put("userId", savedUser.getId());
        event.put("email", savedUser.getEmail());
        event.put("role", savedUser.getRole());
        event.put("occurredAt", LocalDateTime.now().toString());
        kafkaTemplate.send("user.created", savedUser.getEmail(), event);

        // 5. Générer les tokens et retourner
        return generateAuthResponse(savedUser);
    }

    /**
     * CONNEXION D'UN UTILISATEUR
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Tentative de connexion : {}", request.getEmail());

        // Spring Security vérifie email + mot de passe automatiquement
        // Si incorrect → lance une exception BadCredentialsException
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getMotDePasse()
                )
        );

        // Si on arrive ici, l'authentification a réussi
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Publier événement Kafka
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "USER_LOGGED_IN");
        event.put("userId", user.getId());
        event.put("email", user.getEmail());
        event.put("occurredAt", LocalDateTime.now().toString());
        kafkaTemplate.send("user.authenticated", user.getEmail(), event);

        log.info("Connexion réussie : {}", user.getEmail());
        return generateAuthResponse(user);
    }

    /**
     * INFOS DE L'UTILISATEUR CONNECTÉ
     */
    public UserInfo getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return mapToUserInfo(user);
    }

    /**
     * DÉSACTIVER UN COMPTE
     */
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setActif(false);
        userRepository.save(user);
        log.info("Compte désactivé : {}", user.getEmail());
    }

    // ============================================
    // MÉTHODES PRIVÉES
    // ============================================

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(mapToUserInfo(user))
                .build();
    }

    private UserInfo mapToUserInfo(User user) {
        return UserInfo.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .role(user.getRole())
                .actif(user.isActif())
                .build();
    }
}
