CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS cash_operations
(
    operation_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id     UUID           NOT NULL,
    type           VARCHAR(20)    NOT NULL CHECK (type IN ('DEPOSIT', 'WITHDRAWAL')),
    amount         NUMERIC(15, 2) NOT NULL CHECK (amount > 0),
    currency_code  VARCHAR(3)     NOT NULL,
    status         VARCHAR(20)    NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    description    TEXT,
    created_at     TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP,
    completed_at   TIMESTAMPTZ
);

CREATE INDEX idx_cash_operations_account ON cash_operations (account_id);
CREATE INDEX idx_cash_operations_type ON cash_operations (type);
CREATE INDEX idx_cash_operations_status ON cash_operations (status);
CREATE INDEX idx_cash_operations_created_at ON cash_operations (created_at);