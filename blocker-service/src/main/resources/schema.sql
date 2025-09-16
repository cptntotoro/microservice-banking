CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Таблица операций
CREATE TABLE IF NOT EXISTS operation_records
(
    id                UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    operation_id      UUID           NOT NULL UNIQUE,
    operation_type    VARCHAR(50)    NOT NULL,
    user_id           UUID           NOT NULL,
    account_id        UUID           NOT NULL,
    amount            NUMERIC(19, 4) NOT NULL,
    currency          VARCHAR(3)     NOT NULL,
    timestamp         TIMESTAMP      NOT NULL,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    blocked           BOOLEAN        NOT NULL DEFAULT FALSE,
    block_reason_code VARCHAR(50),
    risk_score        INTEGER        NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_operation_records_user_id ON operation_records (user_id);
CREATE INDEX IF NOT EXISTS idx_operation_records_timestamp ON operation_records (timestamp);
CREATE INDEX IF NOT EXISTS idx_operation_records_user_timestamp ON operation_records (user_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_operation_records_operation_type ON operation_records (operation_type);
CREATE INDEX IF NOT EXISTS idx_operation_records_blocked ON operation_records (blocked);