CREATE TABLE trusted_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name VARCHAR(80) NOT NULL,
    method VARCHAR(10) NOT NULL,
    contact_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_trusted_contacts_method CHECK (method IN ('LIGACAO', 'MENSAGEM', 'WHATSAPP', 'EMAIL'))
);

CREATE INDEX idx_trusted_contacts_user ON trusted_contacts (user_id);
