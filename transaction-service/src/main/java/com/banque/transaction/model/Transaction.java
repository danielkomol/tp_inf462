package com.banque.transaction.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ENTITÉ TRANSACTION
 * Représente toute opération financière sur la plateforme.
 * Une transaction est IMMUABLE après confirmation.
 */
@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * RÉFÉRENCE UNIQUE DE LA TRANSACTION
     * Format : TXN-[timestamp]-[random]
     * Ex : TXN-20260614-AB12CD
     */
    @Column(unique = true, nullable = false)
    private String reference;

    /**
     * TYPE DE TRANSACTION
     * DEPOT, RETRAIT, TRANSFERT_INTRA, TRANSFERT_INTER
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    /**
     * STATUT DE LA TRANSACTION
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.EN_COURS;

    /**
     * MONTANT — toujours positif
     * BigDecimal pour éviter les erreurs d'arrondi sur l'argent
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    /**
     * DEVISE
     */
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String devise = "XAF";

    /**
     * FRAIS DE TRANSACTION
     */
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal frais = BigDecimal.ZERO;

    /**
     * COMPTES SOURCE ET DESTINATAIRE
     * On stocke les IDs (pas les objets) car les comptes
     * sont gérés par l'account-service
     */
    @Column(nullable = false)
    private String compteSource;

    // Null pour les dépôts
    private String compteDestinataire;

    /**
     * IDs DES CLIENTS ET OPÉRATEURS
     */
    @Column(nullable = false)
    private String clientSourceId;

    private String clientDestId;

    @Column(nullable = false)
    private String operateurSourceId;

    private String operateurDestId;

    /**
     * DESCRIPTION DE L'OPÉRATION
     */
    private String description;

    /**
     * MOTIF DE REJET (si la transaction échoue)
     */
    private String motifRejet;

    /**
     * DATES
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ============================================
    // ENUMS
    // ============================================

    public enum TransactionType {
        DEPOT,              // Dépôt d'argent
        RETRAIT,            // Retrait d'argent
        TRANSFERT_INTRA,    // Transfert même opérateur
        TRANSFERT_INTER     // Transfert entre opérateurs différents
    }

    public enum TransactionStatus {
        EN_COURS,   // Transaction initiée
        VALIDEE,    // Transaction réussie
        ECHOUEE,    // Transaction échouée
        ANNULEE     // Transaction annulée
    }
}
