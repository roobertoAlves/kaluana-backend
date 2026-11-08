CREATE TABLE guide_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    guide_id VARCHAR(40) NOT NULL,
    guide_version INTEGER NOT NULL,
    status VARCHAR(10) NOT NULL,
    steps_count INTEGER NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_guide_sessions_status CHECK (status IN ('COMPLETED', 'ABANDONED')),
    CONSTRAINT ck_guide_sessions_steps CHECK (steps_count BETWEEN 0 AND 50),
    CONSTRAINT ck_guide_sessions_period CHECK (finished_at >= started_at)
);

CREATE INDEX idx_guide_sessions_user_finished ON guide_sessions (user_id, finished_at DESC);
