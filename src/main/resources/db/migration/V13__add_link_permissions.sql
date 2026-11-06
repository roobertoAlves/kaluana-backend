ALTER TABLE responsible_links
    ADD COLUMN relationship VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    ADD COLUMN share_events BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN share_check_ins BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN share_usage BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN share_tasks BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN share_reports BOOLEAN NOT NULL DEFAULT FALSE,
    ADD CONSTRAINT ck_responsible_links_relationship
        CHECK (relationship IN ('FAMILY', 'HEALTH_PROFESSIONAL', 'OTHER'));

UPDATE responsible_links l
SET share_events = a.share_events,
    share_check_ins = a.share_check_ins,
    share_usage = a.share_usage,
    share_reports = a.share_reports
FROM access_settings a
WHERE a.user_id = l.primary_user_id;
