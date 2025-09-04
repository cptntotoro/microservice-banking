CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Таблица пользователей (аккаунтов)
CREATE TABLE users
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    login          VARCHAR(50)  NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL,
    birth_date     DATE         NOT NULL,
    created_at     TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP,
    enabled        BOOLEAN          DEFAULT TRUE,
    account_locked BOOLEAN          DEFAULT FALSE,
    CONSTRAINT chk_user_age CHECK (EXTRACT(YEAR FROM AGE(birth_date)) >= 18)
);

-- Индексы для таблицы users
CREATE UNIQUE INDEX idx_users_login ON users (login);
CREATE UNIQUE INDEX idx_users_email ON users (email);