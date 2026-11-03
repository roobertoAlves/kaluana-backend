-- RF17: revisão do fim do dia. Guarda só as contagens das decisões e, se a
-- pessoa quiser, como foi o dia em três opções fixas (sem pontuação, RNF6).
CREATE TABLE day_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    review_date DATE NOT NULL,
    feeling VARCHAR(4),
    moved_tomorrow INTEGER NOT NULL DEFAULT 0,
    unscheduled INTEGER NOT NULL DEFAULT 0,
    completed INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_day_reviews_user_date UNIQUE (user_id, review_date),
    CONSTRAINT ck_day_reviews_feeling CHECK (feeling IS NULL OR feeling IN ('HARD', 'OK', 'GOOD')),
    CONSTRAINT ck_day_reviews_counts CHECK (moved_tomorrow >= 0 AND unscheduled >= 0 AND completed >= 0)
);
