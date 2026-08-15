CREATE TABLE push_tokens
(
    id         UUID PRIMARY KEY,
    owner_id   UUID          NOT NULL,
    token      VARCHAR(4096) NOT NULL,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    UNIQUE (owner_id, token)
);

CREATE INDEX idx_push_tokens_owner_id ON push_tokens (owner_id);