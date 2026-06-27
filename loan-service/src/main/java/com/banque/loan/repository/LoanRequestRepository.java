package com.banque.loan.repository;

import com.banque.loan.model.LoanRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRequestRepository extends JpaRepository<LoanRequest, Long> {

    Optional<LoanRequest> findByReference(String reference);
    List<LoanRequest> findByClientId(String clientId);
    List<LoanRequest> findByOperateurId(String operateurId);
    List<LoanRequest> findByStatut(LoanRequest.StatutDemande statut);
    boolean existsByReference(String reference);

    @Query("SELECT COUNT(l) > 0 FROM LoanRequest l WHERE l.clientId = :clientId AND l.statut = 'EN_COURS'")
    boolean hasActiveLoan(String clientId);
}
