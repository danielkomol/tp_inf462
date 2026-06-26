package com.banque.operator.service;

import com.banque.operator.dto.OperatorDTO.*;
import com.banque.operator.model.Operator;
import com.banque.operator.repository.OperatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OperatorService {

    private final OperatorRepository operatorRepository;

    @Transactional
    public OperatorResponse createOperator(CreateOperatorRequest request) {
        log.info("Création de l'opérateur : {}", request.getCode());

        if (operatorRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Ce code opérateur existe déjà : " + request.getCode());
        }

        Operator operator = Operator.builder()
                .code(request.getCode())
                .nom(request.getNom())
                .type(request.getType())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .statut(Operator.StatutOperateur.ACTIF)
                .build();

        // Appliquer les règles personnalisées si fournies
        if (request.getPlafondTransaction() != null) operator.setPlafondTransaction(request.getPlafondTransaction());
        if (request.getPlafondSolde() != null) operator.setPlafondSolde(request.getPlafondSolde());
        if (request.getTauxCommission() != null) operator.setTauxCommission(request.getTauxCommission());
        if (request.getTauxInteretDefaut() != null) operator.setTauxInteretDefaut(request.getTauxInteretDefaut());
        if (request.getPlafondPret() != null) operator.setPlafondPret(request.getPlafondPret());

        Operator saved = operatorRepository.save(operator);
        log.info("Opérateur créé : {}", saved.getNom());
        return mapToResponse(saved);
    }

    public OperatorResponse getByCode(String code) {
        Operator operator = operatorRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Opérateur introuvable : " + code));
        return mapToResponse(operator);
    }

    public OperatorResponse getById(Long id) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opérateur introuvable : " + id));
        return mapToResponse(operator);
    }

    public List<OperatorResponse> getAllActive() {
        return operatorRepository.findByStatut(Operator.StatutOperateur.ACTIF)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<OperatorResponse> getAll() {
        return operatorRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * METTRE À JOUR LES RÈGLES MÉTIER D'UN OPÉRATEUR
     * Plafonds, commissions, taux d'intérêt...
     */
    @Transactional
    public OperatorResponse updateRules(Long id, UpdateRulesRequest request) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opérateur introuvable : " + id));

        if (request.getPlafondTransaction() != null) operator.setPlafondTransaction(request.getPlafondTransaction());
        if (request.getPlafondSolde() != null) operator.setPlafondSolde(request.getPlafondSolde());
        if (request.getTauxCommission() != null) operator.setTauxCommission(request.getTauxCommission());
        if (request.getTauxInteretDefaut() != null) operator.setTauxInteretDefaut(request.getTauxInteretDefaut());
        if (request.getPlafondPret() != null) operator.setPlafondPret(request.getPlafondPret());

        Operator updated = operatorRepository.save(operator);
        log.info("Règles mises à jour pour : {}", updated.getNom());
        return mapToResponse(updated);
    }

    @Transactional
    public OperatorResponse suspendre(Long id) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opérateur introuvable : " + id));
        operator.setStatut(Operator.StatutOperateur.SUSPENDU);
        return mapToResponse(operatorRepository.save(operator));
    }

    @Transactional
    public OperatorResponse reactiver(Long id) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opérateur introuvable : " + id));
        operator.setStatut(Operator.StatutOperateur.ACTIF);
        return mapToResponse(operatorRepository.save(operator));
    }

    private OperatorResponse mapToResponse(Operator o) {
        return OperatorResponse.builder()
                .id(o.getId())
                .code(o.getCode())
                .nom(o.getNom())
                .type(o.getType())
                .email(o.getEmail())
                .telephone(o.getTelephone())
                .plafondTransaction(o.getPlafondTransaction())
                .plafondSolde(o.getPlafondSolde())
                .tauxCommission(o.getTauxCommission())
                .tauxInteretDefaut(o.getTauxInteretDefaut())
                .plafondPret(o.getPlafondPret())
                .statut(o.getStatut())
                .createdAt(o.getCreatedAt())
                .build();
    }
}
