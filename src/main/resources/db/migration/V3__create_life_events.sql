CREATE TABLE life_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category VARCHAR(20) NOT NULL,
    note VARCHAR(500),
    urgent BOOLEAN NOT NULL DEFAULT FALSE,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_life_events_category CHECK (category IN ('SOCIAL', 'IMPULSIVIDADE', 'URGENCIA', 'SOBRECARGA'))
);

CREATE INDEX idx_life_events_user_occurred ON life_events (user_id, occurred_at DESC);
