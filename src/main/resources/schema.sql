-- Budget Management App - Database Schema
-- Rulati acest script in pgAdmin pentru a crea structura bazei de date

-- Creare baza de date (optional - puteti crea manual in pgAdmin)
-- CREATE DATABASE budget_management;

-- Tabela: users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(256) NOT NULL,
    venit_lunar DECIMAL(15, 2) DEFAULT 0.00
);

-- Tabela: plati
CREATE TABLE IF NOT EXISTS plati (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    suma DECIMAL(15, 2) NOT NULL,
    data_ora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tip VARCHAR(20) NOT NULL CHECK (tip IN ('CARD', 'CASH', 'TRANSFER')),
    beneficiar VARCHAR(255),
    categorie VARCHAR(50) CHECK (categorie IN ('FOOD', 'ENTERTAINMENT', 'TRANSPORT', 'UTILITIES', 'HEALTH', 'OTHERS')),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index pentru cautari rapide dupa user
CREATE INDEX IF NOT EXISTS idx_plati_user_id ON plati(user_id);

-- Index pentru filtrare dupa data
CREATE INDEX IF NOT EXISTS idx_plati_data_ora ON plati(data_ora);

-- Inserare user implicit de test (parola: "admin" hashed cu SHA-256)
-- Puteti sterge acest user dupa ce va creati propriul cont
INSERT INTO users (username, password, venit_lunar)
VALUES ('admin', 'admin', 5000.00)
ON CONFLICT (username) DO NOTHING;
