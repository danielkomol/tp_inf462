package com.banque.customer.consumer;

import com.banque.customer.dto.CustomerDTO.CreateCustomerRequest;
import com.banque.customer.repository.CustomerRepository;
import com.banque.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Écoute l'événement "user.registered" publié par l'identity-service.
 * Crée automatiquement le profil Customer correspondant.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserRegisteredConsumer {

    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    @KafkaListener(topics = "user.registered", groupId = "customer-service-group")
    public void onUserRegistered(Map<String, Object> event) {
        try {
            String userId = String.valueOf(event.get("userId"));
            String email  = String.valueOf(event.get("email"));

            // Ne pas créer deux fois
            if (customerRepository.findByUserId(userId).isPresent()) {
                log.info("Profil client déjà existant pour userId: {}", userId);
                return;
            }

            // Extraire nom/prénom depuis l'email si non fournis
            String nom    = event.get("nom")    != null ? String.valueOf(event.get("nom"))    : email.split("@")[0];
            String prenom = event.get("prenom") != null ? String.valueOf(event.get("prenom")) : "";
            String phone  = event.get("phoneNumber") != null ? String.valueOf(event.get("phoneNumber")) : "";

            CreateCustomerRequest req = new CreateCustomerRequest();
            req.setUserId(userId);
            req.setNom(nom);
            req.setPrenom(prenom);
            req.setEmail(email);
            req.setTelephone(phone.isEmpty() ? "+000000000" : phone);

            customerService.createCustomer(req);
            log.info("Profil client créé automatiquement pour userId: {} ({})", userId, email);

        } catch (Exception e) {
            log.error("Erreur création profil client depuis événement Kafka: {}", e.getMessage());
        }
    }
}
