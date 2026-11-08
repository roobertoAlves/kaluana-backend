-- RF9: histórico das sessões de foco. A contagem roda no app; aqui fica
-- só o registro de cada sessão encerrada (sem pontuação, RNF6).
CREATE TABLE timer_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    task_id UUID REFERENCES tasks (id) ON DELETE SET NULL,
    task VARCHAR(80) NOT NULL,
    planned_minutes INTEGER NOT NULL,
    steps_count SMALLINT NOT NULL DEFAULT 0,
    water BOOLEAN NOT NULL DEFAULT FALSE,
    sound VARCHAR(5) NOT NULL DEFAULT 'NONE',
    status VARCHAR(10) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_timer_sessions_status CHECK (status IN ('DONE', 'CANCELED')),
    CONSTRAINT ck_timer_sessions_sound CHECK (sound IN ('NONE', 'WHITE', 'PINK', 'BROWN')),
    CONSTRAINT ck_timer_sessions_minutes CHECK (planned_minutes BETWEEN 1 AND 600),
    CONSTRAINT ck_timer_sessions_period CHECK (finished_at >= started_at)
);

CREATE INDEX idx_timer_sessions_user_finished ON timer_sessions (user_id, finished_at DESC);
