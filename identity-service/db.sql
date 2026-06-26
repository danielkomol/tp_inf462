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

-- Données initiales : un admin par défaut
-- Mot de passe : Admin@1234 (hashé BCrypt)
INSERT INTO users (nom, prenom, email, telephone, mot_de_passe, role)
VALUES (
    'Admin',
    'Système',
    'admin@banque.cm',
    '+237600000000',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhu2',
    'ADMIN'
);
