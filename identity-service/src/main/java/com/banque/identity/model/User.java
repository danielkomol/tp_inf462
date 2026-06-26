package com.banque.identity.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * ENTITE UTILISATEUR
 *
 * Implémente UserDetails de Spring Security :
 * Spring Security utilise cette interface pour gérer
 * l'authentification automatiquement.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    // Email = identifiant de connexion (unique)
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String telephone;

    // Mot de passe stocké HASHÉ (jamais en clair !)
    @Column(nullable = false)
    private String motDePasse;

    // Rôle de l'utilisateur
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.CLIENT;

    // Statut du compte
    @Builder.Default
    private boolean actif = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ============================================
    // METHODES SPRING SECURITY (UserDetails)
    // Spring Security appelle ces méthodes automatiquement
    // ============================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Retourne le rôle sous forme de permission
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    @Override
    public String getUsername() {
        return email; // L'email est le nom d'utilisateur
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return actif; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return actif; }

    // ============================================
    // ENUM ROLES
    // ============================================
    public enum Role {
        CLIENT,         // Utilisateur final
        OPERATEUR,      // Institution financière partenaire
        ADMIN           // Administrateur de la plateforme
    }
}
