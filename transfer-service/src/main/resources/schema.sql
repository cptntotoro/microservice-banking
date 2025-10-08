CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Таблица транзакций (история операций)
CREATE TABLE IF NOT EXISTS transfers
(
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_account_id   UUID           NOT NULL,
    to_account_id     UUID,
    amount            NUMERIC(19, 4) NOT NULL,
    converted_amount  NUMERIC(19, 4),
    from_currency     VARCHAR(3)     NOT NULL,
    to_currency       VARCHAR(3)     NOT NULL,
    timestamp         TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    status            VARCHAR(20)    NOT NULL,
    type              VARCHAR(20)    NOT NULL,
    error_description TEXT
);

-- Индексы для оптимизации (опционально)
CREATE INDEX idx_transfers_from_account_id ON transfers (from_account_id);
CREATE INDEX idx_transfers_to_account_id ON transfers (to_account_id);
CREATE INDEX idx_transfers_timestamp ON transfers (timestamp);
CREATE INDEX idx_transfers_status ON transfers (status);
