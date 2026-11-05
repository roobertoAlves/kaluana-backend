-- Confirmação de e-mail no cadastro: a conta só entra depois do clique no link.
-- Contas que já existiam continuam valendo (DEFAULT TRUE só para elas);
-- as novas são gravadas pelo app com email_verified = FALSE.
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ALTER COLUMN email_verified SET DEFAULT FALSE;

-- Só o hash do token fica no banco; o link vale 24 h e uma única vez.
CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_email_verification_tokens_hash UNIQUE (token_hash)
);

CREATE INDEX idx_email_verification_tokens_user ON email_verification_tokens (user_id);
