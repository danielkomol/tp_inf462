package com.banque.loan.repository;

import com.banque.loan.model.LoanRequest;
import com.banque.loan.model.Echeance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRequestRepository extends JpaRepository<LoanRequest, Long> {
    Optional<LoanRequest> findByReference(String reference);
    List<LoanRequest> findByClientId(String clientId);
    List<LoanRequest> findByOperateurId(String operateurId);
    List<LoanRequest> findByStatut(LoanRequest.StatutDemande statut);
    boolean existsByReference(String reference);

    // Vérifier si un client a déjà un prêt actif
    @Query("SELECT COUNT(l) > 0 FROM LoanRequest l WHERE l.clientId = :clientId AND l.statut = 'EN_COURS'")
    boolean hasActiveLoan(String clientId);
}

@Repository
interface EcheanceRepository extends JpaRepository<Echeance, Long> {
    List<Echeance> findByLoanRequestIdOrderByNumero(Long loanRequestId);

    // Échéances dues aujourd'hui (pour les rappels)
    @Query("SELECT e FROM Echeance e WHERE e.dateEcheance = :date AND e.statut = 'EN_ATTENTE'")
    List<Echeance> findEcheancesDuJour(LocalDate date);

    // Échéances en retard
    @Query("SELECT e FROM Echeance e WHERE e.dateEcheance < :today AND e.statut IN ('EN_ATTENTE', 'DUE')")
    List<Echeance> findEcheancesEnRetard(LocalDate today);
}
