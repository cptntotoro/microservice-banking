-- Включение необходимых расширений
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
    account_non_locked BOOLEAN          DEFAULT TRUE
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

-- Триггер для автоматического обновления updated_at
-- CREATE OR REPLACE FUNCTION update_updated_at_column()
--     RETURNS TRIGGER AS
-- $$
-- BEGIN
--     NEW.updated_at = CURRENT_TIMESTAMP;
--     RETURN NEW;
-- END;
-- $$ language 'plpgsql';
--
-- CREATE TRIGGER trigger_users_updated_at
--     BEFORE UPDATE
--     ON users
--     FOR EACH ROW
-- EXECUTE FUNCTION update_updated_at_column();

-- Таблица валют
CREATE TABLE currencies
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code       VARCHAR(3)  NOT NULL,
    name       VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для таблицы currencies
CREATE UNIQUE INDEX idx_currencies_code ON currencies (code);
CREATE INDEX idx_currencies_name ON currencies (name);

-- Таблица счетов
CREATE TABLE accounts
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    currency_id    UUID        NOT NULL REFERENCES currencies (id),
    balance        NUMERIC(15, 2)   DEFAULT 0.00 CHECK (balance >= 0),
    account_number VARCHAR(20) NOT NULL,
    created_at     TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_currency UNIQUE (user_id, currency_id)
);

-- Индексы для таблицы accounts
CREATE UNIQUE INDEX idx_accounts_number ON accounts (account_number);
CREATE INDEX idx_accounts_user_id ON accounts (user_id);
CREATE INDEX idx_accounts_currency_id ON accounts (currency_id);
CREATE INDEX idx_accounts_created_at ON accounts (created_at);
CREATE INDEX idx_accounts_balance ON accounts (balance);
--
-- -- Триггер для обновления updated_at в accounts
-- CREATE TRIGGER trigger_accounts_updated_at
--     BEFORE UPDATE
--     ON accounts
--     FOR EACH ROW
-- EXECUTE FUNCTION update_updated_at_column();
--
-- Таблица курсов валют
CREATE TABLE exchange_rates
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    base_currency_id   UUID           NOT NULL REFERENCES currencies (id),
    target_currency_id UUID           NOT NULL REFERENCES currencies (id),
    buy_rate           NUMERIC(10, 4) NOT NULL CHECK (buy_rate > 0),
    sell_rate          NUMERIC(10, 4) NOT NULL CHECK (sell_rate > 0),
    effective_date     DATE           NOT NULL,
    created_at         TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_rate_pair_date UNIQUE (base_currency_id, target_currency_id, effective_date)
);

-- Индексы для таблицы exchange_rates
CREATE INDEX idx_exchange_rates_effective_date ON exchange_rates (effective_date);
CREATE INDEX idx_exchange_rates_base_currency ON exchange_rates (base_currency_id);
CREATE INDEX idx_exchange_rates_target_currency ON exchange_rates (target_currency_id);
CREATE INDEX idx_exchange_rates_dates ON exchange_rates (effective_date, created_at);

-- Таблица транзакций (история операций)
CREATE TABLE transactions
(
    id              UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    type            transaction_type NOT NULL,
    from_account_id UUID REFERENCES accounts (id),
    to_account_id   UUID REFERENCES accounts (id),
    amount          NUMERIC(15, 2)   NOT NULL CHECK (amount > 0),
    currency_id     UUID             NOT NULL REFERENCES currencies (id),
    description     TEXT,
    status          transaction_status DEFAULT 'COMPLETED',
    created_at      TIMESTAMPTZ        DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_transfer CHECK (
        (type = 'TRANSFER' AND from_account_id IS NOT NULL AND to_account_id IS NOT NULL) OR
        (type = 'DEPOSIT' AND from_account_id IS NULL AND to_account_id IS NOT NULL) OR
        (type = 'WITHDRAWAL' AND from_account_id IS NOT NULL AND to_account_id IS NULL) OR
        (type = 'EXCHANGE' AND from_account_id IS NOT NULL AND to_account_id IS NOT NULL)
        )
);

-- Индексы для таблицы transactions
CREATE INDEX idx_transactions_from_account ON transactions (from_account_id);
CREATE INDEX idx_transactions_to_account ON transactions (to_account_id);
CREATE INDEX idx_transactions_currency_id ON transactions (currency_id);
CREATE INDEX idx_transactions_created_at ON transactions (created_at);
CREATE INDEX idx_transactions_type ON transactions (type);
CREATE INDEX idx_transactions_status ON transactions (status);
CREATE INDEX idx_transactions_type_created ON transactions (type, created_at);
CREATE INDEX idx_transactions_amount ON transactions (amount);
--
-- -- Частичные индексы для оптимизации
-- CREATE INDEX idx_transactions_recent ON transactions (created_at) WHERE created_at > CURRENT_DATE - INTERVAL '30 days';
-- CREATE INDEX idx_transactions_pending ON transactions (status) WHERE status = 'PENDING';
--
-- -- Функция для проверки баланса при снятии средств
-- CREATE OR REPLACE FUNCTION check_balance()
--     RETURNS TRIGGER AS
-- $$
-- BEGIN
--     IF NEW.type = 'WITHDRAWAL' OR NEW.type = 'TRANSFER' THEN
--         IF (SELECT balance FROM accounts WHERE id = NEW.from_account_id) < NEW.amount THEN
--             RAISE EXCEPTION 'Insufficient funds';
--         END IF;
--     END IF;
--     RETURN NEW;
-- END;
-- $$ LANGUAGE plpgsql;
--
-- -- Триггер для проверки баланса
-- CREATE TRIGGER trigger_check_balance
--     BEFORE INSERT
--     ON transactions
--     FOR EACH ROW
-- EXECUTE FUNCTION check_balance();
--
-- -- Функция для обновления баланса после транзакции
-- CREATE OR REPLACE FUNCTION update_account_balance()
--     RETURNS TRIGGER AS
-- $$
-- BEGIN
--     -- Для снятия средств или перевода
--     IF NEW.from_account_id IS NOT NULL THEN
--         UPDATE accounts
--         SET balance    = balance - NEW.amount,
--             updated_at = CURRENT_TIMESTAMP
--         WHERE id = NEW.from_account_id;
--     END IF;
--
--     -- Для пополнения или получения перевода
--     IF NEW.to_account_id IS NOT NULL THEN
--         UPDATE accounts
--         SET balance    = balance + NEW.amount,
--             updated_at = CURRENT_TIMESTAMP
--         WHERE id = NEW.to_account_id;
--     END IF;
--
--     RETURN NEW;
-- END;
-- $$ LANGUAGE plpgsql;
--
-- -- Триггер для обновления баланса (после успешной вставки)
-- CREATE TRIGGER trigger_update_balance
--     AFTER INSERT
--     ON transactions
--     FOR EACH ROW
--     WHEN (NEW.status = 'COMPLETED')
-- EXECUTE FUNCTION update_account_balance();
--
-- -- Вставка основных валют
-- INSERT INTO currencies (code, name)
-- VALUES ('USD', 'US Dollar'),
--        ('EUR', 'Euro'),
--        ('RUB', 'Russian Ruble'),
--        ('GBP', 'British Pound'),
--        ('JPY', 'Japanese Yen');
--
-- -- Вставка начальных курсов валют
-- INSERT INTO exchange_rates (base_currency_id, target_currency_id, buy_rate, sell_rate, effective_date)
-- SELECT base.id,
--        target.id,
--        75.50,
--        76.20,
--        CURRENT_DATE
-- FROM currencies base,
--      currencies target
-- WHERE base.code = 'USD'
--   AND target.code = 'RUB'
--
-- UNION ALL
--
-- SELECT base.id,
--        target.id,
--        82.30,
--        83.00,
--        CURRENT_DATE
-- FROM currencies base,
--      currencies target
-- WHERE base.code = 'EUR'
--   AND target.code = 'RUB'
--
-- UNION ALL
--
-- SELECT base.id,
--        target.id,
--        0.85,
--        0.86,
--        CURRENT_DATE
-- FROM currencies base,
--      currencies target
-- WHERE base.code = 'USD'
--   AND target.code = 'EUR';