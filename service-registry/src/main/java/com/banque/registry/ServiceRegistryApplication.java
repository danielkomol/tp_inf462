package com.banque.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * SERVICE REGISTRY — Serveur Eureka
 *
 * Tous les microservices s'enregistrent ici au démarrage.
 * La gateway utilise Eureka pour découvrir les services par nom
 * plutôt que par URL en dur (ex: lb://account-service).
 *
 * Dashboard : http://localhost:8761
 */
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
