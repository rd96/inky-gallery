CREATE TABLE IF NOT EXISTS account_tokens (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    token_type VARCHAR(30) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by uuid NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    revoked_at TIMESTAMP NULL,
    CONSTRAINT fk_account_tokens_user_id__id FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_account_tokens_created_by__id FOREIGN KEY (created_by)
        REFERENCES users(id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

ALTER TABLE account_tokens ADD CONSTRAINT account_tokens_token_hash_unique UNIQUE (token_hash);