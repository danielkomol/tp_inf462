-- ============================================================
-- TRANSACTION SERVICE — Schéma MySQL
-- Base de données : transactions_db
-- User : root | Password : (vide)
-- ============================================================

CREATE DATABASE IF NOT EXISTS transactions_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE transactions_db;

CREATE TABLE IF NOT EXISTS transactions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference           VARCHAR(50)     NOT NULL UNIQUE,
    type                ENUM('DEPOT', 'RETRAIT', 'TRANSFERT_INTRA', 'TRANSFERT_INTER') NOT NULL,
    status              ENUM('EN_COURS', 'VALIDEE', 'ECHOUEE', 'ANNULEE') NOT NULL DEFAULT 'EN_COURS',
    montant             DECIMAL(15, 2)  NOT NULL,
    devise              CHAR(3)         NOT NULL DEFAULT 'XAF',
    frais               DECIMAL(15, 2)  NOT NULL DEFAULT 0.00,
    compte_source       VARCHAR(50)     NULL,
    compte_destinataire VARCHAR(50),
    client_source_id    VARCHAR(50)     NOT NULL,
    client_dest_id      VARCHAR(50),
    operateur_source_id VARCHAR(50)     NULL,
    operateur_dest_id   VARCHAR(50),
    description         VARCHAR(500),
    motif_rejet         VARCHAR(500),
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at        DATETIME
);

CREATE INDEX idx_transactions_reference         ON transactions(reference);
CREATE INDEX idx_transactions_client_source     ON transactions(client_source_id);
CREATE INDEX idx_transactions_compte_source     ON transactions(compte_source);
CREATE INDEX idx_transactions_status            ON transactions(status);
CREATE INDEX idx_transactions_created_at        ON transactions(created_at);
