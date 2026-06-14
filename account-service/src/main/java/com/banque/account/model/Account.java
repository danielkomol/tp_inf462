package com.banque.account.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ENTITÉ COMPTE
 *
 * @Entity    → JPA va créer une table "accounts" dans PostgreSQL
 * @Table     → on peut personnaliser le nom de la table
 * @Data      → Lombok génère automatiquement getters, setters, toString, equals
 * @Builder   → permet de créer un objet avec le pattern builder
 * @NoArgsConstructor / @AllArgsConstructor → génère les constructeurs
 */
@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    /**
     * CLÉ PRIMAIRE
     * @Id         → c'est la clé primaire de la table
     * @GeneratedValue → la valeur est générée automatiquement
     * IDENTITY   → PostgreSQL s'occupe de l'auto-incrément
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * NUMÉRO DE COMPTE
     * unique = true    → deux comptes ne peuvent pas avoir le même numéro
     * nullable = false → ce champ est obligatoire
     */
    @Column(unique = true, nullable = false)
    private String accountNumber;

    /**
     * ID DU PROPRIÉTAIRE
     * On stocke seulement l'ID du client (pas l'objet complet)
     * car le client est géré par le customer-service
     */
    @Column(nullable = false)
    private String customerId;

    /**
     * TYPE DE COMPTE
     * ENUM = valeurs prédéfinies : COURANT, EPARGNE, MOBILE_MONEY
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    /**
     * SOLDE
     * BigDecimal = type précis pour l'argent (évite les erreurs d'arrondi)
     * precision = nombre total de chiffres
     * scale = nombre de chiffres après la virgule
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    /**
     * DEVISE
     * XAF = Franc CFA (devise du Cameroun)
     */
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "XAF";

    /**
     * STATUT DU COMPTE
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    /**
     * ID DE L'OPÉRATEUR FINANCIER
     * Chaque compte appartient à un opérateur
     */
    @Column(nullable = false)
    private String operatorId;

    /**
     * PLAFOND DE TRANSACTION
     * Montant maximum autorisé pour une seule transaction
     */
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal transactionLimit = new BigDecimal("1000000");

    /**
     * DATES AUTOMATIQUES
     * @PrePersist → exécuté juste avant la sauvegarde initiale
     * @PreUpdate  → exécuté juste avant chaque mise à jour
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
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
    // ENUMS IMBRIQUÉS
    // ============================================

    /**
     * Types de comptes disponibles sur la plateforme
     */
    public enum AccountType {
        COURANT,        // Compte courant classique
        EPARGNE,        // Compte épargne
        MOBILE_MONEY    // Compte mobile money (Orange Money, MTN MoMo)
    }

    /**
     * Statuts possibles d'un compte
     */
    public enum AccountStatus {
        ACTIVE,     // Compte actif, opérations autorisées
        BLOCKED,    // Compte bloqué temporairement
        CLOSED      // Compte définitivement fermé
    }
}
