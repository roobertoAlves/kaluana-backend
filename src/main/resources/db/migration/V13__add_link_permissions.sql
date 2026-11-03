-- RF8 / RNF3: autorização por pessoa. Cada vínculo (responsável ou
-- profissional) tem o seu próprio "o que pode ver" e o tipo de perfil.
-- access_settings passa a ser o padrão aplicado a quem se conectar depois.
ALTER TABLE responsible_links
    ADD COLUMN relationship VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    ADD COLUMN share_events BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN share_check_ins BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN share_usage BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN share_tasks BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN share_reports BOOLEAN NOT NULL DEFAULT FALSE,
    ADD CONSTRAINT ck_responsible_links_relationship
        CHECK (relationship IN ('FAMILY', 'HEALTH_PROFESSIONAL', 'OTHER'));

-- Vínculos que já existiam mantêm o que a pessoa tinha liberado.
UPDATE responsible_links l
SET share_events = a.share_events,
    share_check_ins = a.share_check_ins,
    share_usage = a.share_usage,
    share_reports = a.share_reports
FROM access_settings a
WHERE a.user_id = l.primary_user_id;
