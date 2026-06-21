package com.banque.loan.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ENTITÉ ÉCHÉANCE
 * Représente une mensualité de remboursement du prêt.
 * Générée automatiquement lors de la validation du prêt.
 */
@Entity
@Table(name = "echeances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Echeance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lien vers la demande de prêt
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_request_id", nullable = false)
    private LoanRequest loanRequest;

    // Numéro de l'échéance (1, 2, 3...)
    @Column(nullable = false)
    private Integer numero;

    // Date limite de paiement
    @Column(nullable = false)
    private LocalDate dateEcheance;

    // Montant total à payer (capital + intérêts)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTotal;

    // Part du capital dans cette mensualité
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal partCapital;

    // Part des intérêts dans cette mensualité
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal partInteret;

    // Capital restant dû après cette échéance
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal capitalRestant;

    // Pénalité de retard (si applicable)
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal penalite = BigDecimal.ZERO;

    // Statut de l'échéance
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutEcheance statut = StatutEcheance.EN_ATTENTE;

    // Date de paiement effectif
    private LocalDateTime datePaiement;

    public enum StatutEcheance {
        EN_ATTENTE,  // Pas encore due
        DUE,         // Date dépassée, pas encore payée
        PAYEE,       // Payée à temps
        EN_RETARD    // Payée avec retard
    }
}
