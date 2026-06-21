package com.banque.operator.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ENTITÉ OPÉRATEUR FINANCIER
 * Représente une institution partenaire (banque, microfinance, mobile money)
 * avec ses propres règles métier configurables.
 */
@Entity
@Table(name = "operators")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Operator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // Ex: "MTN-MOMO", "ORANGE-MONEY", "AFRILAND"

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeOperateur type;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String telephone;

    // RÈGLES MÉTIER CONFIGURABLES PAR OPÉRATEUR

    // Plafond de transaction par défaut
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal plafondTransaction = new BigDecimal("1000000");

    // Plafond de solde de compte
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal plafondSolde = new BigDecimal("10000000");

    // Taux de commission sur transactions (%)
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tauxCommission = new BigDecimal("0.5");

    // Taux d'intérêt par défaut sur les prêts (%)
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tauxInteretDefaut = new BigDecimal("12.0");

    // Montant maximum pour un prêt
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal plafondPret = new BigDecimal("5000000");

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutOperateur statut = StatutOperateur.ACTIF;

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

    public enum TypeOperateur {
        BANQUE,
        MICROFINANCE,
        MOBILE_MONEY
    }

    public enum StatutOperateur {
        ACTIF,
        SUSPENDU,
        INACTIF
    }
}
