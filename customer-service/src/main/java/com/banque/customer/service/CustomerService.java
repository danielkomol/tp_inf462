package com.banque.customer.service;

import com.banque.customer.dto.CustomerDTO.*;
import com.banque.customer.model.Customer;
import com.banque.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        log.info("Création du profil client pour userId : {}", request.getUserId());

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email existe déjà : " + request.getEmail());
        }
        if (customerRepository.existsByTelephone(request.getTelephone())) {
            throw new RuntimeException("Ce téléphone existe déjà");
        }

        Customer customer = Customer.builder()
                .userId(request.getUserId())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .dateNaissance(request.getDateNaissance())
                .lieuNaissance(request.getLieuNaissance())
                .sexe(request.getSexe())
                .adresse(request.getAdresse())
                .ville(request.getVille())
                .numeroCNI(request.getNumeroCNI())
                .typeIdentite(request.getTypeIdentite())
                .profession(request.getProfession())
                .revenuMensuel(request.getRevenuMensuel())
                .statutVerification(Customer.StatutVerification.EN_ATTENTE)
                .scoreCredit(500)
                .build();

        Customer saved = customerRepository.save(customer);

        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "CUSTOMER_CREATED");
        event.put("customerId", saved.getId());
        event.put("userId", saved.getUserId());
        event.put("email", saved.getEmail());
        event.put("occurredAt", LocalDateTime.now().toString());
        kafkaTemplate.send("customer.created", saved.getUserId(), event);

        log.info("Profil client créé : {} {}", saved.getNom(), saved.getPrenom());
        return mapToResponse(saved);
    }

    public CustomerResponse getByUserId(String userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Client introuvable : " + userId));
        return mapToResponse(customer);
    }

    public CustomerResponse getById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client introuvable : " + id));
        return mapToResponse(customer);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client introuvable : " + id));

        if (request.getAdresse() != null) customer.setAdresse(request.getAdresse());
        if (request.getVille() != null) customer.setVille(request.getVille());
        if (request.getProfession() != null) customer.setProfession(request.getProfession());
        if (request.getRevenuMensuel() != null) customer.setRevenuMensuel(request.getRevenuMensuel());
        if (request.getTelephone() != null) customer.setTelephone(request.getTelephone());

        Customer updated = customerRepository.save(customer);
        return mapToResponse(updated);
    }

    @Transactional
    public CustomerResponse verifierKYC(Long id, boolean approuve) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client introuvable : " + id));

        customer.setStatutVerification(approuve
                ? Customer.StatutVerification.VERIFIE
                : Customer.StatutVerification.REJETE);

        Customer updated = customerRepository.save(customer);

        Map<String, Object> event = new HashMap<>();
        event.put("eventType", approuve ? "CUSTOMER_VERIFIED" : "CUSTOMER_REJECTED");
        event.put("customerId", updated.getId());
        event.put("userId", updated.getUserId());
        kafkaTemplate.send("customer.kyc.updated", updated.getUserId(), event);

        return mapToResponse(updated);
    }

    private CustomerResponse mapToResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId())
                .userId(c.getUserId())
                .nom(c.getNom())
                .prenom(c.getPrenom())
                .email(c.getEmail())
                .telephone(c.getTelephone())
                .dateNaissance(c.getDateNaissance())
                .ville(c.getVille())
                .numeroCNI(c.getNumeroCNI())
                .profession(c.getProfession())
                .revenuMensuel(c.getRevenuMensuel())
                .statutVerification(c.getStatutVerification())
                .scoreCredit(c.getScoreCredit())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
