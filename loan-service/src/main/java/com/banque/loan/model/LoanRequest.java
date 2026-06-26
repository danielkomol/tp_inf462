package com.banque.loan.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ENTITÉ DEMANDE DE PRÊT
 * Représente le cycle complet d'un prêt :
 * SOUMISE → EN_ANALYSE → VALIDEE/REJETEE → EN_COURS → SOLDEE
 */
@Entity
@Table(name = "loan_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Référence unique du dossier
    @Column(unique = true, nullable = false)
    private String reference;

    // ID du client demandeur
    @Column(nullable = false)
    private String clientId;

    // ID de l'opérateur financier
    @Column(nullable = false)
    private String operateurId;

    // Montant demandé
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montantDemande;

    // Durée en mois
    @Column(nullable = false)
    private Integer duree;

    // Taux d'intérêt annuel (%)
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxInteret;

    // Montant accordé (peut différer du montant demandé)
    @Column(precision = 15, scale = 2)
    private BigDecimal montantAccorde;

    // Motif de la demande
    @Column(nullable = false)
    private String motif;

    // Statut du dossier
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutDemande statut = StatutDemande.SOUMISE;

    // Motif de rejet (si rejeté)
    private String motifRejet;

    // Compte sur lequel verser le prêt
    @Column(nullable = false)
    private String compteVersement;

    // Échéances de remboursement (relation OneToMany)
    @OneToMany(mappedBy = "loanRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Echeance> echeances;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime validatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // STATUTS POSSIBLES
    public enum StatutDemande {
        SOUMISE,      // Dossier soumis, en attente d'analyse
        EN_ANALYSE,   // Dossier en cours d'analyse
        VALIDEE,      // Prêt approuvé
        REJETEE,      // Prêt refusé
        EN_COURS,     // Prêt en cours de remboursement
        SOLDEE        // Prêt entièrement remboursé
    }
}
