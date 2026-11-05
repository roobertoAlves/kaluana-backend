ALTER TABLE timer_sessions ALTER COLUMN sound TYPE VARCHAR(6);
ALTER TABLE timer_sessions DROP CONSTRAINT ck_timer_sessions_sound;
ALTER TABLE timer_sessions ADD CONSTRAINT ck_timer_sessions_sound
    CHECK (sound IN ('NONE', 'WHITE', 'PINK', 'BROWN', 'RAIN', 'WAVES', 'FIRE', 'FOREST'));

CREATE TABLE ambient_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    mode VARCHAR(5) NOT NULL,
    sound VARCHAR(6) NOT NULL,
    planned_minutes INTEGER NOT NULL,
    status VARCHAR(10) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_ambient_sessions_mode CHECK (mode IN ('RELAX', 'SLEEP')),
    CONSTRAINT ck_ambient_sessions_sound CHECK (sound IN ('WHITE', 'PINK', 'BROWN', 'RAIN', 'WAVES', 'FIRE', 'FOREST')),
    CONSTRAINT ck_ambient_sessions_status CHECK (status IN ('DONE', 'CANCELED')),
    CONSTRAINT ck_ambient_sessions_minutes CHECK (planned_minutes BETWEEN 1 AND 120),
    CONSTRAINT ck_ambient_sessions_period CHECK (finished_at >= started_at)
);

CREATE INDEX idx_ambient_sessions_user_finished ON ambient_sessions (user_id, finished_at DESC);
