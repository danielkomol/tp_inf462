-- ============================================================
-- ACCOUNT SERVICE — Schéma MySQL
-- Base de données : accounts_db
-- User : root | Password : (vide)
-- ============================================================

CREATE DATABASE IF NOT EXISTS accounts_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE accounts_db;

CREATE TABLE IF NOT EXISTS accounts (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number    VARCHAR(20)     NOT NULL UNIQUE,
    customer_id       VARCHAR(50)     NOT NULL,
    account_type      ENUM('COURANT', 'EPARGNE', 'MOBILE_MONEY') NOT NULL,
    balance           DECIMAL(15, 2)  NOT NULL DEFAULT 0.00,
    currency          CHAR(3)         NOT NULL DEFAULT 'XAF',
    status            ENUM('ACTIVE', 'BLOCKED', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    operator_id       VARCHAR(50)     NOT NULL,
    transaction_limit DECIMAL(15, 2)  NOT NULL DEFAULT 1000000.00,
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_accounts_customer   ON accounts(customer_id);
CREATE INDEX idx_accounts_operator   ON accounts(operator_id);
CREATE INDEX idx_accounts_status     ON accounts(status);
CREATE INDEX idx_accounts_number     ON accounts(account_number);
