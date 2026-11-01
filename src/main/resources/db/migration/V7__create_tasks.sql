-- RF5: tarefas com microetapas (checklist).
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title VARCHAR(120) NOT NULL,
    priority VARCHAR(6) NOT NULL,
    estimated_minutes INTEGER,
    due_date DATE,
    status VARCHAR(4) NOT NULL DEFAULT 'TODO',
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_tasks_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT ck_tasks_status CHECK (status IN ('TODO', 'DONE')),
    CONSTRAINT ck_tasks_minutes CHECK (estimated_minutes IS NULL OR estimated_minutes BETWEEN 5 AND 480),
    CONSTRAINT ck_tasks_completed CHECK ((status = 'DONE') = (completed_at IS NOT NULL))
);

CREATE INDEX idx_tasks_user_status_due ON tasks (user_id, status, due_date);

CREATE TABLE task_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    text VARCHAR(120) NOT NULL,
    position SMALLINT NOT NULL,
    done BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_task_steps_task ON task_steps (task_id, position);
