package com.banque.account.repository;

import com.banque.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY DES COMPTES
 *
 * JpaRepository<Account, Long> signifie :
 *   - Account = l'entité gérée
 *   - Long    = le type de la clé primaire (id)
 *
 * En héritant de JpaRepository, tu obtiens GRATUITEMENT :
 *   save()        → créer ou modifier un compte
 *   findById()    → chercher par ID
 *   findAll()     → récupérer tous les comptes
 *   deleteById()  → supprimer par ID
 *   count()       → compter le nombre de comptes
 *   ... et bien d'autres !
 *
 * @Repository → dit à Spring que c'est un composant de données
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * REQUÊTES PERSONNALISÉES
     * Spring comprend le nom de la méthode et génère le SQL automatiquement !
     * findBy[NomDuChamp] → WHERE nom_du_champ = ?
     */

    // SELECT * FROM accounts WHERE account_number = ?
    Optional<Account> findByAccountNumber(String accountNumber);

    // SELECT * FROM accounts WHERE customer_id = ?
    List<Account> findByCustomerId(String customerId);

    // SELECT * FROM accounts WHERE status = ?
    List<Account> findByStatus(Account.AccountStatus status);

    // SELECT * FROM accounts WHERE operator_id = ?
    List<Account> findByOperatorId(String operatorId);

    // SELECT * FROM accounts WHERE customer_id = ? AND status = ?
    List<Account> findByCustomerIdAndStatus(String customerId, Account.AccountStatus status);

    // Vérifier si un numéro de compte existe déjà
    boolean existsByAccountNumber(String accountNumber);

    /**
     * REQUÊTE JPQL PERSONNALISÉE
     * Pour les requêtes plus complexes, on écrit le JPQL manuellement
     * JPQL = Java Persistence Query Language (comme SQL mais avec les noms de classes Java)
     */
    @Query("SELECT a FROM Account a WHERE a.customerId = :customerId AND a.accountType = :type")
    List<Account> findByCustomerIdAndType(String customerId, Account.AccountType type);
}
