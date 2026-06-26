-- ============================================================
-- CUSTOMER SERVICE — Schéma MySQL
-- Base de données : customers_db
-- User : root | Password : (vide)
-- ============================================================

CREATE DATABASE IF NOT EXISTS customers_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE customers_db;

CREATE TABLE IF NOT EXISTS customers (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              VARCHAR(50)     NOT NULL UNIQUE,
    nom                  VARCHAR(100)    NOT NULL,
    prenom               VARCHAR(100)    NOT NULL,
    email                VARCHAR(255)    NOT NULL UNIQUE,
    telephone            VARCHAR(20)     NOT NULL UNIQUE,
    date_naissance       DATE,
    lieu_naissance       VARCHAR(200),
    sexe                 ENUM('MASCULIN', 'FEMININ'),
    adresse              VARCHAR(500),
    ville                VARCHAR(100),
    numero_cni           VARCHAR(50),
    type_identite        ENUM('CNI', 'PASSEPORT', 'CARTE_SEJOUR'),
    profession           VARCHAR(200),
    revenu_mensuel       DOUBLE,
    statut_verification  ENUM('EN_ATTENTE', 'VERIFIE', 'REJETE') NOT NULL DEFAULT 'EN_ATTENTE',
    score_credit         INT             NOT NULL DEFAULT 500,
    created_at           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_customers_user_id  ON customers(user_id);
CREATE INDEX idx_customers_email    ON customers(email);
CREATE INDEX idx_customers_statut   ON customers(statut_verification);
