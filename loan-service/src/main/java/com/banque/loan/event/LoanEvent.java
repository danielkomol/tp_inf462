package com.banque.loan.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanEvent {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LoanSubmitted {
        private String eventType = "LOAN_SUBMITTED";
        private Long loanId;
        private String reference;
        private String clientId;
        private String operateurId;
        private BigDecimal montantDemande;
        private Integer duree;
        private String motif;
        private LocalDateTime occurredAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LoanValidated {
        private String eventType = "LOAN_VALIDATED";
        private Long loanId;
        private String reference;
        private String clientId;
        private String operateurId;
        private BigDecimal montantAccorde;
        private Integer duree;
        private BigDecimal tauxInteret;
        private String compteVersement;
        private LocalDateTime occurredAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LoanRejected {
        private String eventType = "LOAN_REJECTED";
        private Long loanId;
        private String reference;
        private String clientId;
        private String motifRejet;
        private LocalDateTime occurredAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RepaymentDone {
        private String eventType = "REPAYMENT_DONE";
        private Long loanId;
        private Long echeanceId;
        private String clientId;
        private Integer numeroEcheance;
        private BigDecimal montantPaye;
        private LocalDateTime occurredAt;
    }
}
