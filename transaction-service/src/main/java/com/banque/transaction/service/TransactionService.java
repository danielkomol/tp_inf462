package com.banque.transaction.service;

import com.banque.transaction.dto.TransactionDTO.*;
import com.banque.transaction.event.TransactionEvent;
import com.banque.transaction.model.Transaction;
import com.banque.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * SERVICE TRANSACTIONS — Logique métier
 *
 * RÈGLES IMPORTANTES :
 * 1. Toute transaction est atomique (@Transactional)
 * 2. Une transaction validée est IMMUABLE
 * 3. Les frais sont calculés automatiquement
 * 4. Chaque opération publie un événement Kafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Topics Kafka
    private static final String TOPIC_VALIDATED = "transaction.validated";
    private static final String TOPIC_FAILED = "transaction.failed";
    private static final String TOPIC_INTER = "transaction.inter.initiated";

    // Frais de transaction (0.5% du montant)
    private static final BigDecimal TAUX_FRAIS = new BigDecimal("0.005");

    /**
     * DÉPÔT D'ARGENT
     */
    @Transactional
    public TransactionResponse depot(DepotRequest request) {
        log.info("Dépôt de {} XAF sur le compte {}", request.getMontant(), request.getCompteDestinataire());

        // Créer la transaction
        Transaction transaction = Transaction.builder()
                .reference(generateReference())
                .type(Transaction.TransactionType.DEPOT)
                .status(Transaction.TransactionStatus.EN_COURS)
                .montant(request.getMontant())
                .frais(BigDecimal.ZERO)
                .compteSource("CAISSE")
                .compteDestinataire(request.getCompteDestinataire())
                .clientSourceId(request.getClientId())   // pour satisfaire la contrainte NOT NULL
                .clientDestId(request.getClientId())
                .operateurSourceId(request.getOperateurId() != null ? request.getOperateurId() : "SYS")
                .description(request.getDescription())
                .build();

        transaction = transactionRepository.save(transaction);

        // Valider la transaction
        transaction.setStatus(Transaction.TransactionStatus.VALIDEE);
        transaction.setCompletedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);

        // Publier événement Kafka
        publierTransactionValidee(transaction);

        log.info("Dépôt réussi : {}", transaction.getReference());
        return mapToResponse(transaction);
    }

    /**
     * RETRAIT D'ARGENT
     */
    @Transactional
    public TransactionResponse retrait(RetraitRequest request) {
        log.info("Retrait de {} XAF du compte {}", request.getMontant(), request.getCompteSource());

        // Calculer les frais
        BigDecimal frais = request.getMontant().multiply(TAUX_FRAIS);

        Transaction transaction = Transaction.builder()
                .reference(generateReference())
                .type(Transaction.TransactionType.RETRAIT)
                .status(Transaction.TransactionStatus.EN_COURS)
                .montant(request.getMontant())
                .frais(frais)
                .compteSource(request.getCompteSource())
                .compteDestinataire("CAISSE")
                .clientSourceId(request.getClientId())
                .operateurSourceId(request.getOperateurId())
                .description(request.getDescription())
                .build();

        transaction = transactionRepository.save(transaction);

        // Valider
        transaction.setStatus(Transaction.TransactionStatus.VALIDEE);
        transaction.setCompletedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);

        publierTransactionValidee(transaction);

        log.info("Retrait réussi : {}", transaction.getReference());
        return mapToResponse(transaction);
    }

    /**
     * TRANSFERT INTRA-OPÉRATEUR
     * Les deux comptes appartiennent au même opérateur
     */
    @Transactional
    public TransactionResponse transfertIntra(TransfertRequest request) {
        log.info("Transfert intra de {} vers {} : {} XAF",
                request.getCompteSource(), request.getCompteDestinataire(), request.getMontant());

        // Vérifier que ce n'est pas un transfert vers soi-même
        if (request.getCompteSource().equals(request.getCompteDestinataire())) {
            throw new RuntimeException("Impossible de transférer vers le même compte");
        }

        BigDecimal frais = request.getMontant().multiply(TAUX_FRAIS);

        Transaction transaction = Transaction.builder()
                .reference(generateReference())
                .type(Transaction.TransactionType.TRANSFERT_INTRA)
                .status(Transaction.TransactionStatus.EN_COURS)
                .montant(request.getMontant())
                .frais(frais)
                .compteSource(request.getCompteSource())
                .compteDestinataire(request.getCompteDestinataire())
                .clientSourceId(request.getClientSourceId())
                .clientDestId(request.getClientDestId())
                .operateurSourceId(request.getOperateurSourceId())
                .operateurDestId(request.getOperateurSourceId())
                .description(request.getDescription())
                .build();

        transaction = transactionRepository.save(transaction);
        transaction.setStatus(Transaction.TransactionStatus.VALIDEE);
        transaction.setCompletedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);

        publierTransactionValidee(transaction);

        log.info("Transfert intra réussi : {}", transaction.getReference());
        return mapToResponse(transaction);
    }

    /**
     * TRANSFERT INTER-OPÉRATEUR
     * Les comptes appartiennent à des opérateurs différents
     * Utilise le pattern Saga pour garantir la cohérence
     */
    @Transactional
    public TransactionResponse transfertInter(TransfertRequest request) {
        log.info("Transfert inter-opérateur : {} XAF de {} vers {}",
                request.getMontant(), request.getOperateurSourceId(), request.getOperateurDestId());

        // Frais plus élevés pour inter-opérateur (1%)
        BigDecimal frais = request.getMontant().multiply(new BigDecimal("0.01"));

        Transaction transaction = Transaction.builder()
                .reference(generateReference())
                .type(Transaction.TransactionType.TRANSFERT_INTER)
                .status(Transaction.TransactionStatus.EN_COURS)
                .montant(request.getMontant())
                .frais(frais)
                .compteSource(request.getCompteSource())
                .compteDestinataire(request.getCompteDestinataire())
                .clientSourceId(request.getClientSourceId())
                .clientDestId(request.getClientDestId())
                .operateurSourceId(request.getOperateurSourceId())
                .operateurDestId(request.getOperateurDestId())
                .description(request.getDescription())
                .build();

        transaction = transactionRepository.save(transaction);

        // Publier événement Saga — les autres services vont traiter
        TransactionEvent.InterTransferInitiated sagaEvent = TransactionEvent.InterTransferInitiated.builder()
                .transactionId(transaction.getId())
                .reference(transaction.getReference())
                .montant(transaction.getMontant())
                .compteSource(transaction.getCompteSource())
                .compteDestinataire(transaction.getCompteDestinataire())
                .operateurSourceId(transaction.getOperateurSourceId())
                .operateurDestId(transaction.getOperateurDestId())
                .occurredAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send(TOPIC_INTER, transaction.getReference(), sagaEvent);

        // Valider après publication
        transaction.setStatus(Transaction.TransactionStatus.VALIDEE);
        transaction.setCompletedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);

        publierTransactionValidee(transaction);

        log.info("Transfert inter réussi : {}", transaction.getReference());
        return mapToResponse(transaction);
    }

    /**
     * CONSULTER UNE TRANSACTION PAR RÉFÉRENCE
     */
    public TransactionResponse getByReference(String reference) {
        Transaction transaction = transactionRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Transaction introuvable : " + reference));
        return mapToResponse(transaction);
    }

    /**
     * HISTORIQUE DES TRANSACTIONS D'UN CLIENT
     */
    public List<TransactionResponse> getHistoriqueClient(String clientId) {
        return transactionRepository.findAllByClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * HISTORIQUE DES TRANSACTIONS D'UN COMPTE
     */
    public List<TransactionResponse> getHistoriqueCompte(String numeroCompte) {
        List<Transaction> source = transactionRepository.findByCompteSourceOrderByCreatedAtDesc(numeroCompte);
        List<Transaction> dest = transactionRepository.findByCompteDestinataireOrderByCreatedAtDesc(numeroCompte);
        source.addAll(dest);
        return source.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ============================================
    // MÉTHODES PRIVÉES
    // ============================================

    /**
     * Génère une référence unique
     * Format : TXN-20260614-AB12CD
     */
    private String generateReference() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%06X", new Random().nextInt(0xFFFFFF));
        String reference = "TXN-" + date + "-" + random;

        // Vérifier l'unicité
        while (transactionRepository.existsByReference(reference)) {
            random = String.format("%06X", new Random().nextInt(0xFFFFFF));
            reference = "TXN-" + date + "-" + random;
        }
        return reference;
    }

    /**
     * Publie un événement TransactionValidated sur Kafka
     */
    private void publierTransactionValidee(Transaction transaction) {
        TransactionEvent.TransactionValidated event = TransactionEvent.TransactionValidated.builder()
                .transactionId(transaction.getId())
                .reference(transaction.getReference())
                .type(transaction.getType().name())
                .montant(transaction.getMontant())
                .frais(transaction.getFrais())
                .compteSource(transaction.getCompteSource())
                .compteDestinataire(transaction.getCompteDestinataire())
                .clientSourceId(transaction.getClientSourceId())
                .clientDestId(transaction.getClientDestId())
                .description(transaction.getDescription())
                .occurredAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send(TOPIC_VALIDATED, transaction.getReference(), event);
    }

    /**
     * Convertit une Transaction en TransactionResponse
     */
    private TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .reference(t.getReference())
                .type(t.getType())
                .status(t.getStatus())
                .montant(t.getMontant())
                .frais(t.getFrais())
                .devise(t.getDevise())
                .compteSource(t.getCompteSource())
                .compteDestinataire(t.getCompteDestinataire())
                .description(t.getDescription())
                .motifRejet(t.getMotifRejet())
                .createdAt(t.getCreatedAt())
                .completedAt(t.getCompletedAt())
                .build();
    }
}
