package com.banque.transaction.repository;

import com.banque.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY TRANSACTIONS
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Chercher par référence unique
    Optional<Transaction> findByReference(String reference);

    // Toutes les transactions d'un compte source
    List<Transaction> findByCompteSourceOrderByCreatedAtDesc(String compteSource);

    // Toutes les transactions d'un compte destinataire
    List<Transaction> findByCompteDestinataireOrderByCreatedAtDesc(String compteDestinataire);

    // Historique complet d'un client (source OU destinataire)
    @Query("SELECT t FROM Transaction t WHERE t.clientSourceId = :clientId OR t.clientDestId = :clientId ORDER BY t.createdAt DESC")
    List<Transaction> findAllByClientId(String clientId);

    // Transactions par statut
    List<Transaction> findByStatus(Transaction.TransactionStatus status);

    // Transactions entre deux dates
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :debut AND :fin ORDER BY t.createdAt DESC")
    List<Transaction> findByPeriode(LocalDateTime debut, LocalDateTime fin);

    // Vérifier si référence existe
    boolean existsByReference(String reference);
}
