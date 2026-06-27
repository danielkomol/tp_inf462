package com.banque.loan.repository;

import com.banque.loan.model.Echeance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EcheanceRepository extends JpaRepository<Echeance, Long> {

    List<Echeance> findByLoanRequestIdOrderByNumero(Long loanRequestId);

    @Query("SELECT e FROM Echeance e WHERE e.dateEcheance = :date AND e.statut = 'EN_ATTENTE'")
    List<Echeance> findEcheancesDuJour(LocalDate date);

    @Query("SELECT e FROM Echeance e WHERE e.dateEcheance < :today AND e.statut IN ('EN_ATTENTE', 'DUE')")
    List<Echeance> findEcheancesEnRetard(LocalDate today);
}
