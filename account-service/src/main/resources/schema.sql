CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users
(
    user_uuid          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username           VARCHAR(50)  NOT NULL UNIQUE,
    password_hash      VARCHAR(255) NOT NULL,
    first_name         VARCHAR(100) NOT NULL,
    last_name          VARCHAR(100) NOT NULL,
    email              VARCHAR(150) NOT NULL UNIQUE,
    birth_date         DATE         NOT NULL,
    created_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    enabled            BOOLEAN          DEFAULT TRUE,
    account_non_locked BOOLEAN          DEFAULT TRUE,
    CONSTRAINT chk_user_age CHECK (EXTRACT(YEAR FROM AGE(birth_date)) >= 18)
);

CREATE UNIQUE INDEX idx_users_username ON users (username);
CREATE UNIQUE INDEX idx_users_email ON users (email);

-- Таблица ролей
CREATE TABLE IF NOT EXISTS roles
(
    role_uuid   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    description TEXT,
    name        VARCHAR(20) NOT NULL UNIQUE
);

-- Таблица связи пользователей и ролей
CREATE TABLE IF NOT EXISTS user_roles
(
    user_uuid UUID NOT NULL,
    role_uuid UUID NOT NULL,
    PRIMARY KEY (user_uuid, role_uuid),
    FOREIGN KEY (user_uuid) REFERENCES users (user_uuid) ON DELETE CASCADE,
    FOREIGN KEY (role_uuid) REFERENCES roles (role_uuid) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user_id ON user_roles (user_uuid);
CREATE INDEX idx_user_roles_role_id ON user_roles (role_uuid);

-- Таблица валют
CREATE TABLE IF NOT EXISTS currencies
(
    currency_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code          VARCHAR(3)  NOT NULL,
    name          VARCHAR(50) NOT NULL
);

CREATE UNIQUE INDEX idx_currencies_code ON currencies (code);
CREATE INDEX idx_currencies_name ON currencies (name);

-- Таблица счетов
CREATE TABLE IF NOT EXISTS accounts
(
    account_uuid   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID        NOT NULL REFERENCES users (user_uuid) ON DELETE CASCADE,
    currency_id    UUID        NOT NULL REFERENCES currencies (currency_uuid),
    balance        NUMERIC(15, 2)   DEFAULT 0.00 CHECK (balance >= 0),
    created_at     TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_currency UNIQUE (user_id, currency_id)
);

-- CREATE UNIQUE INDEX idx_accounts_number ON accounts (account_number);
CREATE INDEX idx_accounts_user_id ON accounts (user_id);
CREATE INDEX idx_accounts_currency_id ON accounts (currency_id);