ALTER TABLE users
    ADD COLUMN nickname VARCHAR(30),
    ADD COLUMN avatar_color VARCHAR(7),
    ADD CONSTRAINT ck_users_avatar_color CHECK (avatar_color IS NULL OR avatar_color ~ '^#[0-9A-Fa-f]{6}$');
