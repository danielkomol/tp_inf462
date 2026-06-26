-- ============================================================
-- LOAN SERVICE — Schéma MySQL
-- Base de données : loans_db
-- User : root | Password : (vide)
-- ============================================================

CREATE DATABASE IF NOT EXISTS loans_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE loans_db;

CREATE TABLE IF NOT EXISTS loan_requests (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference         VARCHAR(50)     NOT NULL UNIQUE,
    client_id         VARCHAR(50)     NOT NULL,
    operateur_id      VARCHAR(50)     NOT NULL,
    montant_demande   DECIMAL(15, 2)  NOT NULL,
    duree             INT             NOT NULL,
    taux_interet      DECIMAL(5, 2)   NOT NULL,
    montant_accorde   DECIMAL(15, 2),
    motif             VARCHAR(500)    NOT NULL,
    statut            ENUM('SOUMISE', 'EN_ANALYSE', 'VALIDEE', 'REJETEE', 'EN_COURS', 'SOLDEE') NOT NULL DEFAULT 'SOUMISE',
    motif_rejet       VARCHAR(500),
    compte_versement  VARCHAR(50)     NOT NULL,
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    validated_at      DATETIME
);

CREATE TABLE IF NOT EXISTS echeances (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_request_id  BIGINT          NOT NULL,
    numero           INT             NOT NULL,
    date_echeance    DATE            NOT NULL,
    montant_total    DECIMAL(15, 2)  NOT NULL,
    part_capital     DECIMAL(15, 2)  NOT NULL,
    part_interet     DECIMAL(15, 2)  NOT NULL,
    capital_restant  DECIMAL(15, 2)  NOT NULL,
    penalite         DECIMAL(15, 2)  NOT NULL DEFAULT 0.00,
    statut           ENUM('EN_ATTENTE', 'DUE', 'PAYEE', 'EN_RETARD') NOT NULL DEFAULT 'EN_ATTENTE',
    date_paiement    DATETIME,
    CONSTRAINT fk_echeance_loan FOREIGN KEY (loan_request_id) REFERENCES loan_requests(id) ON DELETE CASCADE
);

CREATE INDEX idx_loans_client     ON loan_requests(client_id);
CREATE INDEX idx_loans_statut     ON loan_requests(statut);
CREATE INDEX idx_echeances_loan   ON echeances(loan_request_id);
CREATE INDEX idx_echeances_statut ON echeances(statut);
