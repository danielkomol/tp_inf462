package com.banque.account.service;

import com.banque.account.dto.AccountDTO.*;
import com.banque.account.event.AccountEvent;
import com.banque.account.model.Account;
import com.banque.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * SERVICE DES COMPTES — LOGIQUE MÉTIER
 *
 * @Service    → dit à Spring que c'est un composant de service
 * @Slf4j      → ajoute un logger automatiquement (variable "log")
 * @RequiredArgsConstructor → génère un constructeur avec les champs "final"
 *                            (alternative à @Autowired)
 * @Transactional → si une opération échoue, toutes les modifications
 *                  en base de données sont annulées (rollback)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

    // Injection automatique par Spring
    private final AccountRepository accountRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Noms des topics Kafka
    private static final String TOPIC_ACCOUNT_CREATED = "account.created";
    private static final String TOPIC_ACCOUNT_CREDITED = "account.credited";
    private static final String TOPIC_ACCOUNT_DEBITED = "account.debited";
    private static final String TOPIC_STATUS_CHANGED = "account.status.changed";

    /**
     * CRÉER UN NOUVEAU COMPTE
     *
     * @Transactional = si quelque chose plante, la BDD revient à son état initial
     */
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Création d'un compte pour le client : {}", request.getCustomerId());

        // 1. Générer un numéro de compte unique
        String accountNumber = generateAccountNumber(request.getAccountType());

        // 2. Définir le solde initial (0 si non fourni)
        BigDecimal initialBalance = request.getInitialBalance() != null
                ? request.getInitialBalance()
                : BigDecimal.ZERO;

        // 3. Créer l'entité Account avec le pattern Builder
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .customerId(request.getCustomerId())
                .accountType(request.getAccountType())
                .balance(initialBalance)
                .currency(request.getCurrency() != null ? request.getCurrency() : "XAF")
                .status(Account.AccountStatus.ACTIVE)
                .operatorId(request.getOperatorId())
                .build();

        // 4. Sauvegarder en base de données
        Account savedAccount = accountRepository.save(account);
        log.info("Compte créé avec succès : {}", savedAccount.getAccountNumber());

        // 5. Publier l'événement sur Kafka (non bloquant)
        try {
            AccountEvent.AccountCreated event = AccountEvent.AccountCreated.builder()
                    .accountId(savedAccount.getId())
                    .accountNumber(savedAccount.getAccountNumber())
                    .customerId(savedAccount.getCustomerId())
                    .accountType(savedAccount.getAccountType().name())
                    .initialBalance(savedAccount.getBalance())
                    .currency(savedAccount.getCurrency())
                    .operatorId(savedAccount.getOperatorId())
                    .occurredAt(LocalDateTime.now())
                    .build();
            kafkaTemplate.send(TOPIC_ACCOUNT_CREATED, savedAccount.getCustomerId(), event);
            log.info("Événement AccountCreated publié sur Kafka pour le compte : {}", accountNumber);
        } catch (Exception e) {
            log.warn("Kafka indisponible, événement AccountCreated ignoré : {}", e.getMessage());
        }

        // 6. Retourner la réponse (DTO, pas l'entité directement)
        return mapToResponse(savedAccount);
    }

    /**
     * CONSULTER UN COMPTE PAR ID
     */
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                // Si le compte n'existe pas, on lance une exception
                .orElseThrow(() -> new RuntimeException("Compte introuvable avec l'ID : " + id));
        return mapToResponse(account);
    }

    /**
     * CONSULTER UN COMPTE PAR NUMÉRO
     */
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Compte introuvable : " + accountNumber));
        return mapToResponse(account);
    }

    /**
     * LISTER TOUS LES COMPTES D'UN CLIENT
     */
    public List<AccountResponse> getAllActive() {
        return accountRepository.findByStatus(Account.AccountStatus.ACTIVE)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<AccountResponse> getAccountsByCustomer(String customerId) {
        return accountRepository.findByCustomerId(customerId)
                .stream()
                // Pour chaque compte, on le convertit en AccountResponse
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * CRÉDITER UN COMPTE (déposer de l'argent)
     *
     * @Transactional garantit que le débit et la mise à jour sont atomiques
     */
    @Transactional
    public AccountResponse creditAccount(Long accountId, CreditRequest request) {
        log.info("Crédit du compte {} : +{} XAF", accountId, request.getAmount());

        // 1. Récupérer le compte
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Compte introuvable : " + accountId));

        // 2. Vérifier que le compte est actif
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new RuntimeException("Impossible de créditer un compte " + account.getStatus());
        }

        // 3. Ajouter le montant au solde
        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);

        // 4. Sauvegarder
        Account updatedAccount = accountRepository.save(account);

        // 5. Publier l'événement Kafka
        try {
            AccountEvent.AccountCredited event = AccountEvent.AccountCredited.builder()
                    .accountId(updatedAccount.getId())
                    .accountNumber(updatedAccount.getAccountNumber())
                    .customerId(updatedAccount.getCustomerId())
                    .amount(request.getAmount())
                    .newBalance(newBalance)
                    .description(request.getDescription())
                    .occurredAt(LocalDateTime.now())
                    .build();
            kafkaTemplate.send(TOPIC_ACCOUNT_CREDITED, updatedAccount.getCustomerId(), event);
        } catch (Exception e) {
            log.warn("Kafka indisponible, événement AccountCredited ignoré : {}", e.getMessage());
        }

        return mapToResponse(updatedAccount);
    }

    /**
     * DÉBITER UN COMPTE (retirer de l'argent)
     */
    @Transactional
    public AccountResponse debitAccount(Long accountId, DebitRequest request) {
        log.info("Débit du compte {} : -{} XAF", accountId, request.getAmount());

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Compte introuvable : " + accountId));

        // Vérification 1 : compte actif
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new RuntimeException("Impossible de débiter un compte " + account.getStatus());
        }

        // Vérification 2 : solde suffisant
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Solde insuffisant. Solde actuel : " + account.getBalance() + " XAF");
        }

        // Vérification 3 : plafond de transaction
        if (request.getAmount().compareTo(account.getTransactionLimit()) > 0) {
            throw new RuntimeException("Montant dépasse le plafond autorisé : " + account.getTransactionLimit() + " XAF");
        }

        // Soustraire le montant
        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);

        Account updatedAccount = accountRepository.save(account);

        // Publier l'événement Kafka
        try {
            AccountEvent.AccountDebited event = AccountEvent.AccountDebited.builder()
                    .accountId(updatedAccount.getId())
                    .accountNumber(updatedAccount.getAccountNumber())
                    .customerId(updatedAccount.getCustomerId())
                    .amount(request.getAmount())
                    .newBalance(newBalance)
                    .description(request.getDescription())
                    .occurredAt(LocalDateTime.now())
                    .build();
            kafkaTemplate.send(TOPIC_ACCOUNT_DEBITED, updatedAccount.getCustomerId(), event);
        } catch (Exception e) {
            log.warn("Kafka indisponible, événement AccountDebited ignoré : {}", e.getMessage());
        }

        return mapToResponse(updatedAccount);
    }

    /**
     * BLOQUER UN COMPTE
     */
    @Transactional
    public AccountResponse blockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Compte introuvable : " + accountId));

        String oldStatus = account.getStatus().name();
        account.setStatus(Account.AccountStatus.BLOCKED);
        Account updated = accountRepository.save(account);

        publishStatusChanged(updated, oldStatus);
        return mapToResponse(updated);
    }

    /**
     * FERMER UN COMPTE
     */
    @Transactional
    public AccountResponse closeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Compte introuvable : " + accountId));

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Impossible de fermer un compte avec un solde positif");
        }

        String oldStatus = account.getStatus().name();
        account.setStatus(Account.AccountStatus.CLOSED);
        Account updated = accountRepository.save(account);

        publishStatusChanged(updated, oldStatus);
        return mapToResponse(updated);
    }

    // ============================================
    // MÉTHODES PRIVÉES (utilitaires internes)
    // ============================================

    /**
     * Génère un numéro de compte unique
     * Format : [PREFIX][10 chiffres aléatoires]
     */
    private String generateAccountNumber(Account.AccountType type) {
        String prefix = switch (type) {
            case COURANT -> "CC";
            case EPARGNE -> "CE";
            case MOBILE_MONEY -> "MM";
        };

        String number;
        do {
            // Génère 10 chiffres aléatoires
            number = prefix + String.format("%010d", new Random().nextLong(10_000_000_000L));
        } while (accountRepository.existsByAccountNumber(number)); // Vérifie l'unicité

        return number;
    }

    /**
     * Publie un événement de changement de statut sur Kafka
     */
    private void publishStatusChanged(Account account, String oldStatus) {
        try {
            AccountEvent.AccountStatusChanged event = AccountEvent.AccountStatusChanged.builder()
                    .accountId(account.getId())
                    .accountNumber(account.getAccountNumber())
                    .customerId(account.getCustomerId())
                    .oldStatus(oldStatus)
                    .newStatus(account.getStatus().name())
                    .occurredAt(LocalDateTime.now())
                    .build();
            kafkaTemplate.send(TOPIC_STATUS_CHANGED, account.getCustomerId(), event);
        } catch (Exception e) {
            log.warn("Kafka indisponible, événement StatusChanged ignoré : {}", e.getMessage());
        }
    }

    /**
     * Convertit une entité Account en AccountResponse (DTO)
     * On ne renvoie jamais l'entité directement !
     */
    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomerId())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .operatorId(account.getOperatorId())
                .transactionLimit(account.getTransactionLimit())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
