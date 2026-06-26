package com.banque.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * POINT D'ENTRÉE DE L'APPLICATION
 *
 * @SpringBootApplication fait 3 choses en une :
 *   1. @Configuration      → ce fichier peut configurer des beans Spring
 *   2. @EnableAutoConfiguration → Spring configure automatiquement tout ce qu'il trouve
 *   3. @ComponentScan      → Spring va chercher toutes tes classes dans ce package
 *
 * La méthode main() est la première chose exécutée quand tu lances le service.
 */
@SpringBootApplication
public class AccountServiceApplication {

    public static void main(String[] args) {
        // Cette ligne démarre le serveur Spring Boot
        // Elle lit le fichier application.properties, configure la BDD, Kafka, etc.
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
