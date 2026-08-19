INSERT INTO account_tokens (
    id,
    user_id,
    token_type,
    token_hash,
    created_at,
    created_by,
    expires_at,
    used_at,
    revoked_at
)
SELECT
    id,
    user_id,
    'ACTIVATION',
    token_hash,
    created_at,
    created_by,
    expires_at,
    used_at,
    revoked_at
FROM activation_tokens;

DROP TABLE activation_tokens;