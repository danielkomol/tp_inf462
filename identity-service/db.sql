-- ============================================================
-- IDENTITY SERVICE — Schéma MySQL
-- Base de données : identity_db
-- User : root | Password : (vide)
-- ============================================================

CREATE DATABASE IF NOT EXISTS identity_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE identity_db;

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom           VARCHAR(100)  NOT NULL,
    prenom        VARCHAR(100)  NOT NULL,
    email         VARCHAR(255)  NOT NULL UNIQUE,
    telephone     VARCHAR(20)   NOT NULL UNIQUE,
    mot_de_passe  VARCHAR(255)  NOT NULL,
    role          ENUM('CLIENT', 'OPERATEUR', 'ADMIN') NOT NULL DEFAULT 'CLIENT',
    actif         TINYINT(1)    NOT NULL DEFAULT 1,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Index pour accélérer la recherche par email (login)
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role  ON users(role);

-- Données initiales
-- Admin : admin@banque.cm / Admin1234!
-- Operateur : operateur@banque.cm / Admin1234!
INSERT INTO users (id, email, password, phone_number, role, enabled, two_factor_enabled, otp_enabled, created_at, updated_at)
VALUES (UUID(), 'admin@banque.cm', '$2a$12$pBGJLAZF7T6Aq5FudKvMbOk63RbjX0kgh/p448IS4glam19Z9cwgu', '+237600000000', 'ADMIN', 1, 0, 0, NOW(), NOW());

INSERT INTO users (id, email, password, phone_number, role, enabled, two_factor_enabled, otp_enabled, created_at, updated_at)
VALUES (UUID(), 'operateur@banque.cm', '$2a$12$pBGJLAZF7T6Aq5FudKvMbOk63RbjX0kgh/p448IS4glam19Z9cwgu', '+237600000001', 'OPERATEUR', 1, 0, 0, NOW(), NOW());
