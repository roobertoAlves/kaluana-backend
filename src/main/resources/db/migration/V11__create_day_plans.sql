-- RF10 / RF14: plano do dia salvo, para poder ser replanejado quando o dia muda.
CREATE TABLE day_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    plan_date DATE NOT NULL,
    window_end TIME NOT NULL,
    source VARCHAR(9) NOT NULL,
    energy_level INTEGER,
    note VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_day_plans_user_date UNIQUE (user_id, plan_date),
    CONSTRAINT ck_day_plans_source CHECK (source IN ('RULES', 'ASSISTANT')),
    CONSTRAINT ck_day_plans_energy CHECK (energy_level IS NULL OR energy_level BETWEEN 1 AND 5)
);

-- Fotografia do plano aceito: sem FK para tasks, porque uma tarefa apagada
-- precisa continuar aparecendo como "removida" no próximo replanejamento.
CREATE TABLE day_plan_blocks (
    plan_id UUID NOT NULL REFERENCES day_plans (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    kind VARCHAR(5) NOT NULL,
    task_id UUID,
    title VARCHAR(120) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    PRIMARY KEY (plan_id, position),
    CONSTRAINT ck_day_plan_blocks_kind CHECK (kind IN ('TASK', 'BREAK')),
    CONSTRAINT ck_day_plan_blocks_task CHECK ((kind = 'TASK') = (task_id IS NOT NULL)),
    CONSTRAINT ck_day_plan_blocks_time CHECK (start_time < end_time)
);

CREATE TABLE day_plan_unscheduled (
    plan_id UUID NOT NULL REFERENCES day_plans (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    task_id UUID NOT NULL,
    title VARCHAR(120) NOT NULL,
    minutes INTEGER NOT NULL,
    PRIMARY KEY (plan_id, position)
);
