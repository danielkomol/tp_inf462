package com.banque.loan.service;

import com.banque.loan.dto.LoanDTO;
import com.banque.loan.dto.LoanDTO.LoanResponse;
import com.banque.loan.dto.LoanDTO.LoanSubmitRequest;
import com.banque.loan.dto.LoanDTO.ValidationRequest;
import com.banque.loan.dto.LoanDTO.EcheanceResponse;
import com.banque.loan.event.LoanEvent;
import com.banque.loan.model.Echeance;
import com.banque.loan.model.LoanRequest;
import com.banque.loan.repository.LoanRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanService {

    private final LoanRequestRepository loanRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final BigDecimal TAUX_DEFAUT = new BigDecimal("12.0");

    @Transactional
    public LoanResponse soumettreDemande(LoanSubmitRequest request) {
        log.info("Nouvelle demande de prêt pour client : {}", request.getClientId());

        if (loanRepository.hasActiveLoan(request.getClientId())) {
            throw new RuntimeException("Vous avez déjà un prêt en cours.");
        }

        LoanRequest loan = LoanRequest.builder()
                .reference(genererReference())
                .clientId(request.getClientId())
                .operateurId(request.getOperateurId())
                .montantDemande(request.getMontantDemande())
                .duree(request.getDuree())
                .tauxInteret(TAUX_DEFAUT)
                .motif(request.getMotif())
                .compteVersement(request.getCompteVersement())
                .statut(LoanRequest.StatutDemande.SOUMISE)
                .build();

        loan = loanRepository.save(loan);

        LoanEvent.LoanSubmitted event = LoanEvent.LoanSubmitted.builder()
                .loanId(loan.getId())
                .reference(loan.getReference())
                .clientId(loan.getClientId())
                .operateurId(loan.getOperateurId())
                .montantDemande(loan.getMontantDemande())
                .duree(loan.getDuree())
                .motif(loan.getMotif())
                .occurredAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send("loan.submitted", loan.getClientId(), event);
        return mapToResponse(loan);
    }

    @Transactional
    public LoanResponse validerDemande(Long loanId, ValidationRequest request) {
        LoanRequest loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable : " + loanId));

        if (loan.getStatut() != LoanRequest.StatutDemande.SOUMISE &&
            loan.getStatut() != LoanRequest.StatutDemande.EN_ANALYSE) {
            throw new RuntimeException("Cette demande ne peut plus être modifiée.");
        }

        if (Boolean.TRUE.equals(request.getApprouve())) {
            BigDecimal montantAccorde = request.getMontantAccorde() != null
                    ? request.getMontantAccorde() : loan.getMontantDemande();
            BigDecimal taux = request.getTauxInteret() != null
                    ? request.getTauxInteret() : TAUX_DEFAUT;

            loan.setMontantAccorde(montantAccorde);
            loan.setTauxInteret(taux);
            loan.setStatut(LoanRequest.StatutDemande.VALIDEE);
            loan.setValidatedAt(LocalDateTime.now());

            List<Echeance> echeances = genererEcheancier(loan, montantAccorde, taux, loan.getDuree());
            loan.setEcheances(echeances);
            loan.setStatut(LoanRequest.StatutDemande.EN_COURS);
            loan = loanRepository.save(loan);

            LoanEvent.LoanValidated event = LoanEvent.LoanValidated.builder()
                    .loanId(loan.getId()).reference(loan.getReference())
                    .clientId(loan.getClientId()).operateurId(loan.getOperateurId())
                    .montantAccorde(montantAccorde).duree(loan.getDuree())
                    .tauxInteret(taux).compteVersement(loan.getCompteVersement())
                    .occurredAt(LocalDateTime.now()).build();

            kafkaTemplate.send("loan.validated", loan.getClientId(), event);
        } else {
            loan.setStatut(LoanRequest.StatutDemande.REJETEE);
            loan.setMotifRejet(request.getMotifRejet());
            loan = loanRepository.save(loan);

            LoanEvent.LoanRejected event = LoanEvent.LoanRejected.builder()
                    .loanId(loan.getId()).reference(loan.getReference())
                    .clientId(loan.getClientId()).motifRejet(request.getMotifRejet())
                    .occurredAt(LocalDateTime.now()).build();

            kafkaTemplate.send("loan.rejected", loan.getClientId(), event);
        }

        return mapToResponse(loan);
    }

    public LoanResponse getDemande(String reference) {
        LoanRequest loan = loanRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Demande introuvable : " + reference));
        return mapToResponse(loan);
    }

    public List<LoanResponse> getDemandesClient(String clientId) {
        return loanRepository.findByClientId(clientId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<LoanResponse> getDemandesEnAttente(String operateurId) {
        return loanRepository.findByStatut(LoanRequest.StatutDemande.SOUMISE)
                .stream()
                .filter(l -> l.getOperateurId().equals(operateurId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LoanResponse> getAll() {
        return loanRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Génération de l'échéancier (méthode française) ────────────────
    private List<Echeance> genererEcheancier(LoanRequest loan, BigDecimal capital,
                                              BigDecimal tauxAnnuel, int duree) {
        List<Echeance> echeances = new ArrayList<>();

        BigDecimal tauxMensuel = tauxAnnuel
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        BigDecimal unPlusTaux = BigDecimal.ONE.add(tauxMensuel);
        BigDecimal puissance = unPlusTaux.pow(duree, new MathContext(10));
        BigDecimal mensualite = capital
                .multiply(tauxMensuel.multiply(puissance))
                .divide(puissance.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);

        BigDecimal capitalRestant = capital;
        LocalDate dateEcheance = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= duree; i++) {
            BigDecimal interetsMois = capitalRestant
                    .multiply(tauxMensuel).setScale(2, RoundingMode.HALF_UP);
            BigDecimal partCapital = mensualite.subtract(interetsMois);

            if (i == duree) {
                partCapital = capitalRestant;
                mensualite = partCapital.add(interetsMois);
            }

            capitalRestant = capitalRestant.subtract(partCapital);

            echeances.add(Echeance.builder()
                    .loanRequest(loan).numero(i).dateEcheance(dateEcheance)
                    .montantTotal(mensualite).partCapital(partCapital)
                    .partInteret(interetsMois)
                    .capitalRestant(capitalRestant.max(BigDecimal.ZERO))
                    .statut(Echeance.StatutEcheance.EN_ATTENTE)
                    .build());

            dateEcheance = dateEcheance.plusMonths(1);
        }

        return echeances;
    }

    private String genererReference() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String ref;
        do {
            ref = "PRE-" + date + "-" + String.format("%06X", new Random().nextInt(0xFFFFFF));
        } while (loanRepository.existsByReference(ref));
        return ref;
    }

    private LoanResponse mapToResponse(LoanRequest loan) {
        List<EcheanceResponse> echeancesResponse = loan.getEcheances() != null
                ? loan.getEcheances().stream().map(e -> EcheanceResponse.builder()
                        .id(e.getId()).numero(e.getNumero()).dateEcheance(e.getDateEcheance())
                        .montantTotal(e.getMontantTotal()).partCapital(e.getPartCapital())
                        .partInteret(e.getPartInteret()).capitalRestant(e.getCapitalRestant())
                        .penalite(e.getPenalite()).statut(e.getStatut())
                        .datePaiement(e.getDatePaiement()).build())
                        .collect(Collectors.toList())
                : new ArrayList<>();

        return LoanResponse.builder()
                .id(loan.getId()).reference(loan.getReference())
                .clientId(loan.getClientId()).operateurId(loan.getOperateurId())
                .montantDemande(loan.getMontantDemande()).montantAccorde(loan.getMontantAccorde())
                .duree(loan.getDuree()).tauxInteret(loan.getTauxInteret())
                .motif(loan.getMotif()).statut(loan.getStatut())
                .motifRejet(loan.getMotifRejet()).compteVersement(loan.getCompteVersement())
                .echeances(echeancesResponse).createdAt(loan.getCreatedAt())
                .validatedAt(loan.getValidatedAt())
                .build();
    }
}
