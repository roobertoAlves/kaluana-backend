CREATE TABLE check_ins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    day DATE NOT NULL,
    level SMALLINT NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_check_ins_level CHECK (level BETWEEN 1 AND 5),
    CONSTRAINT uk_check_ins_user_day UNIQUE (user_id, day)
);
