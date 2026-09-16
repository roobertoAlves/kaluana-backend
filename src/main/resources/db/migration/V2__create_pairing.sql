CREATE TABLE pairing_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(12) NOT NULL,
    primary_user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_pairing_codes_codigo UNIQUE (code)
);

CREATE INDEX idx_pairing_codes_primary_user ON pairing_codes (primary_user_id);

CREATE TABLE responsible_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    responsible_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    primary_user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    access_level VARCHAR(20) NOT NULL DEFAULT 'DEFAULT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_responsible_links UNIQUE (responsible_id, primary_user_id)
);

CREATE INDEX idx_responsible_links_primary_user ON responsible_links (primary_user_id);
