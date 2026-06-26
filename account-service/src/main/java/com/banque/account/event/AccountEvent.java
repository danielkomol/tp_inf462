package com.banque.account.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ÉVÉNEMENTS KAFKA
 *
 * Ces classes représentent les messages publiés sur Kafka.
 * Quand quelque chose d'important se passe dans account-service,
 * on crée un événement et on l'envoie sur le bon "topic" Kafka.
 *
 * Les autres services (notification, audit...) écoutent ces topics
 * et réagissent en conséquence.
 */
public class AccountEvent {

    /**
     * ÉVÉNEMENT : COMPTE CRÉÉ
     * Publié sur le topic "account.created"
     * Consommé par : notification-service, audit-service
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountCreated {
        private String eventType = "ACCOUNT_CREATED";
        private Long accountId;
        private String accountNumber;
        private String customerId;
        private String accountType;
        private BigDecimal initialBalance;
        private String currency;
        private String operatorId;
        private LocalDateTime occurredAt;
    }

    /**
     * ÉVÉNEMENT : COMPTE CRÉDITÉ
     * Publié sur le topic "account.credited"
     * Consommé par : notification-service, audit-service
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountCredited {
        private String eventType = "ACCOUNT_CREDITED";
        private Long accountId;
        private String accountNumber;
        private String customerId;
        private BigDecimal amount;
        private BigDecimal newBalance;
        private String description;
        private LocalDateTime occurredAt;
    }

    /**
     * ÉVÉNEMENT : COMPTE DÉBITÉ
     * Publié sur le topic "account.debited"
     * Consommé par : notification-service, audit-service
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountDebited {
        private String eventType = "ACCOUNT_DEBITED";
        private Long accountId;
        private String accountNumber;
        private String customerId;
        private BigDecimal amount;
        private BigDecimal newBalance;
        private String description;
        private LocalDateTime occurredAt;
    }

    /**
     * ÉVÉNEMENT : STATUT MODIFIÉ
     * Publié sur le topic "account.status.changed"
     * Consommé par : notification-service, audit-service, transaction-service
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountStatusChanged {
        private String eventType = "ACCOUNT_STATUS_CHANGED";
        private Long accountId;
        private String accountNumber;
        private String customerId;
        private String oldStatus;
        private String newStatus;
        private LocalDateTime occurredAt;
    }
}
