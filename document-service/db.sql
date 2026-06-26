-- ============================================================
-- DOCUMENT SERVICE — Schéma MySQL
-- Base de données : documents_db
-- User : root | Password : (vide)
-- ============================================================

CREATE DATABASE IF NOT EXISTS documents_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE documents_db;

CREATE TABLE IF NOT EXISTS documents (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    client_id           VARCHAR(50)     NOT NULL,
    type_document       VARCHAR(50)     NOT NULL,
    chemin_fichier      VARCHAR(500)    NOT NULL,
    nom_original        VARCHAR(255)    NOT NULL,
    statut              ENUM('EN_ATTENTE', 'EN_COURS_OCR', 'TRAITE', 'INVALIDE') NOT NULL DEFAULT 'EN_ATTENTE',
    donnees_extraites   TEXT,
    niveau_confiance    FLOAT,
    erreur              TEXT,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_documents_client  ON documents(client_id);
CREATE INDEX idx_documents_statut  ON documents(statut);
CREATE INDEX idx_documents_type    ON documents(type_document);
