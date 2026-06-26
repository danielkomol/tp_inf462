package com.banque.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * SERVICE CONFIG — Serveur de configuration centralisée
 *
 * Fournit la configuration de tous les microservices depuis un dépôt Git.
 * Chaque service récupère sa config au démarrage via :
 *   GET http://service-config:8888/{service-name}/{profile}
 *
 * Dépôt Git de config : https://github.com/danielkomol/tp_inf462
 */
@SpringBootApplication
@EnableConfigServer
public class ServiceConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceConfigApplication.class, args);
    }
}
