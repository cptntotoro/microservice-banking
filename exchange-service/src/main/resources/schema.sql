CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Таблица истории конвертации валют
CREATE TABLE IF NOT EXISTS operations
(
    id               UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    user_id          UUID           NOT NULL,
    from_currency    VARCHAR(3)     NOT NULL,
    to_currency      VARCHAR(3)     NOT NULL,
    amount           NUMERIC(15, 4) NOT NULL,
    converted_amount NUMERIC(15, 4) NOT NULL,
    exchange_rate    NUMERIC(10, 6) NOT NULL,
    operation_type   VARCHAR(10)    NOT NULL,
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_operations_currency_pair ON operations (from_currency, to_currency);
CREATE INDEX IF NOT EXISTS idx_operations_created_at ON operations (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_operations_user_id ON operations (user_id);
