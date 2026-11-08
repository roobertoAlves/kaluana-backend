CREATE TABLE access_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    primary_user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    viewer_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    action VARCHAR(12) NOT NULL,
    period_days INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_access_logs_action CHECK (action IN ('VIEW_REPORT', 'EXPORT_PDF')),
    CONSTRAINT ck_access_logs_period CHECK (period_days BETWEEN 1 AND 365)
);

CREATE INDEX idx_access_logs_primary_created ON access_logs (primary_user_id, created_at DESC);
