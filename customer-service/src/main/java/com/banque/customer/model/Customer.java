package com.banque.customer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ENTITÉ CLIENT
 * Représente le profil complet d'un client de la plateforme.
 * Lié à l'identity-service via userId (1 utilisateur = 1 profil client)
 */
@Entity
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lien vers l'utilisateur dans identity-service
    @Column(unique = true, nullable = false)
    private String userId;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String telephone;

    private LocalDate dateNaissance;

    private String lieuNaissance;

    @Enumerated(EnumType.STRING)
    private Sexe sexe;

    private String adresse;

    private String ville;

    // Numéro de pièce d'identité
    private String numeroCNI;

    @Enumerated(EnumType.STRING)
    private TypeIdentite typeIdentite;

    // Profession (utile pour l'analyse de prêt)
    private String profession;

    // Revenu mensuel déclaré (utile pour l'analyse de prêt)
    private Double revenuMensuel;

    // Statut de vérification KYC (Know Your Customer)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutVerification statutVerification = StatutVerification.EN_ATTENTE;

    // Score de crédit (calculé selon l'historique)
    @Builder.Default
    private Integer scoreCredit = 500; // Score initial neutre (0-1000)

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

    public enum Sexe { MASCULIN, FEMININ }

    public enum TypeIdentite { CNI, PASSEPORT, CARTE_SEJOUR }

    public enum StatutVerification {
        EN_ATTENTE,   // Documents pas encore vérifiés
        VERIFIE,      // KYC complété avec succès
        REJETE        // Documents invalides
    }
}
