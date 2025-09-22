CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Таблица уведомлений
CREATE TABLE IF NOT EXISTS notifications
(
    notification_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID         NOT NULL,
    type              VARCHAR(50)  NOT NULL CHECK (type IN ('EMAIL', 'SMS', 'PUSH', 'SYSTEM')),
    subject           VARCHAR(200),
    message           TEXT         NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    created_at        TIMESTAMPTZ    DEFAULT CURRENT_TIMESTAMP,
    sent_at           TIMESTAMPTZ
);

CREATE INDEX idx_notifications_user ON notifications (user_id);
CREATE INDEX idx_notifications_type ON notifications (type);
CREATE INDEX idx_notifications_status ON notifications (status);
CREATE INDEX idx_notifications_created_at ON notifications (created_at);

-- Таблица шаблонов уведомлений
CREATE TABLE IF NOT EXISTS notification_templates
(
    template_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL UNIQUE,
    type          VARCHAR(50)  NOT NULL,
    subject       VARCHAR(200),
    body          TEXT         NOT NULL,
    is_active     BOOLEAN      DEFAULT TRUE,
    created_at    TIMESTAMPTZ    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_templates_type ON notification_templates (type);
CREATE INDEX idx_notification_templates_active ON notification_templates (is_active);