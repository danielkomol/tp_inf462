-- ============================================================
-- OPERATOR SERVICE — Schéma MySQL
-- Base de données : operators_db
-- User : root | Password : (vide)
-- ============================================================

CREATE DATABASE IF NOT EXISTS operators_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE operators_db;

CREATE TABLE IF NOT EXISTS operators (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    code                  VARCHAR(50)     NOT NULL UNIQUE,
    nom                   VARCHAR(200)    NOT NULL,
    type                  ENUM('BANQUE', 'MICROFINANCE', 'MOBILE_MONEY') NOT NULL,
    email                 VARCHAR(255)    NOT NULL,
    telephone             VARCHAR(20)     NOT NULL,
    plafond_transaction   DECIMAL(15, 2)  NOT NULL DEFAULT 1000000.00,
    plafond_solde         DECIMAL(15, 2)  NOT NULL DEFAULT 10000000.00,
    taux_commission       DECIMAL(5, 2)   NOT NULL DEFAULT 0.50,
    taux_interet_defaut   DECIMAL(5, 2)   NOT NULL DEFAULT 12.00,
    plafond_pret          DECIMAL(15, 2)  NOT NULL DEFAULT 5000000.00,
    statut                ENUM('ACTIF', 'SUSPENDU', 'INACTIF') NOT NULL DEFAULT 'ACTIF',
    created_at            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Données initiales : opérateurs de démonstration
INSERT INTO operators (code, nom, type, email, telephone) VALUES
    ('MTN-MOMO',      'MTN Mobile Money Cameroun',  'MOBILE_MONEY', 'momo@mtn.cm',      '+237670000001'),
    ('ORANGE-MONEY',  'Orange Money Cameroun',       'MOBILE_MONEY', 'money@orange.cm',  '+237690000002'),
    ('AFRILAND',      'Afriland First Bank',         'BANQUE',       'info@afriland.cm', '+237222000003'),
    ('CCA-BANK',      'CCA Bank Cameroun',           'BANQUE',       'info@ccabank.cm',  '+237222000004');

CREATE INDEX idx_operators_code   ON operators(code);
CREATE INDEX idx_operators_statut ON operators(statut);
