package com.banque.transaction.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ÉVÉNEMENTS KAFKA — Transaction Service
 *
 * Ces événements sont publiés sur Kafka quand
 * quelque chose d'important se passe dans ce service.
 */
public class TransactionEvent {

    /**
     * TRANSACTION VALIDÉE
     * Topic : transaction.validated
     * Consommé par : notification-service, audit-service, account-service
     */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TransactionValidated {
        private String eventType = "TRANSACTION_VALIDATED";
        private Long transactionId;
        private String reference;
        private String type;
        private BigDecimal montant;
        private BigDecimal frais;
        private String compteSource;
        private String compteDestinataire;
        private String clientSourceId;
        private String clientDestId;
        private String description;
        private LocalDateTime occurredAt;
    }

    /**
     * TRANSACTION ÉCHOUÉE
     * Topic : transaction.failed
     * Consommé par : notification-service, audit-service
     */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TransactionFailed {
        private String eventType = "TRANSACTION_FAILED";
        private Long transactionId;
        private String reference;
        private String type;
        private BigDecimal montant;
        private String compteSource;
        private String clientSourceId;
        private String motifEchec;
        private LocalDateTime occurredAt;
    }

    /**
     * TRANSFERT INTER-OPÉRATEUR INITIÉ
     * Topic : transaction.inter.initiated
     * Consommé par : audit-service (pattern Saga)
     */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class InterTransferInitiated {
        private String eventType = "INTER_TRANSFER_INITIATED";
        private Long transactionId;
        private String reference;
        private BigDecimal montant;
        private String compteSource;
        private String compteDestinataire;
        private String operateurSourceId;
        private String operateurDestId;
        private LocalDateTime occurredAt;
    }
}
