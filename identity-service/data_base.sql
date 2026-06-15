-- ============================================================
--  Identity Service — Schéma de base de données
--  PostgreSQL
-- ============================================================

-- Création de la base et de l'utilisateur (à exécuter en tant que superuser)
-- CREATE DATABASE identity_db;
-- CREATE USER bankapp WITH PASSWORD 'bankapp_pass';
-- GRANT ALL PRIVILEGES ON DATABASE identity_db TO bankapp;
-- GRANT ALL ON SCHEMA public TO bankapp;

-- ============================================================
--  ENUM : rôles utilisateur
-- ============================================================

CREATE TYPE role_enum AS ENUM ('CLIENT', 'ADMIN', 'OPERATEUR', 'AGENT');

-- ============================================================
--  TABLE : users
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id                  VARCHAR(36)  PRIMARY KEY,          -- UUID
    email               VARCHAR(255) NOT NULL UNIQUE,
    password            VARCHAR(255) NOT NULL,              -- bcrypt hash
    phone_number        VARCHAR(20)  UNIQUE,
    role                role_enum    NOT NULL DEFAULT 'CLIENT',
    enabled             BOOLEAN      NOT NULL DEFAULT TRUE,

    -- 2FA (TOTP / Google Authenticator)
    two_factor_enabled  BOOLEAN      NOT NULL DEFAULT FALSE,
    two_factor_secret   VARCHAR(255),                       -- clé base32 TOTP

    -- OTP par SMS / email
    otp_enabled         BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============================================================
--  TABLE : refresh_tokens
-- ============================================================

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          VARCHAR(36)  PRIMARY KEY,                   -- UUID
    token       VARCHAR(512) NOT NULL UNIQUE,
    user_id     VARCHAR(36)  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE
);

-- ============================================================
--  INDEX
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_users_email        ON users(email);
CREATE INDEX IF NOT EXISTS idx_refresh_token      ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_user       ON refresh_tokens(user_id);

-- ============================================================
--  TRIGGER : mise à jour automatique de updated_at
-- ============================================================

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- ============================================================
--  DONNÉES INITIALES : compte admin par défaut
--  mot de passe : Admin@1234  (bcrypt rounds=12)
-- ============================================================

INSERT INTO users (id, email, password, role, enabled)
VALUES (
    gen_random_uuid(),
    'admin@bankapp.com',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC',
    'ADMIN',
    TRUE
) ON CONFLICT (email) DO NOTHING;


GRANT ALL ON SCHEMA public TO bankapp;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO bankapp;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO bankapp;

-- Pour les futures tables créées
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO bankapp;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO bankapp;
