CREATE TABLE reminders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title VARCHAR(80) NOT NULL,
    reminder_time TIME NOT NULL,
    lead_minutes INTEGER NOT NULL DEFAULT 0,
    frequency VARCHAR(10) NOT NULL,
    insistence VARCHAR(10) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_reminders_lead CHECK (lead_minutes BETWEEN 0 AND 1440),
    CONSTRAINT ck_reminders_frequency CHECK (frequency IN ('ONCE', 'DAILY', 'WEEKLY')),
    CONSTRAINT ck_reminders_insistence CHECK (insistence IN ('LOW', 'MEDIUM', 'HIGH'))
);

CREATE INDEX idx_reminders_user ON reminders (user_id);
