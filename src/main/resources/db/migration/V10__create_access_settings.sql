-- RF8 / RNF3: o que o responsável ou profissional emparelhado pode ver.
-- Tudo começa desligado; a ausência de linha equivale a "nada compartilhado".
CREATE TABLE access_settings (
    user_id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    share_events BOOLEAN NOT NULL DEFAULT FALSE,
    share_check_ins BOOLEAN NOT NULL DEFAULT FALSE,
    share_usage BOOLEAN NOT NULL DEFAULT FALSE,
    share_reports BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
