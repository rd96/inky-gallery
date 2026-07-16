ALTER TABLE users ADD display_name VARCHAR(100) NULL;
ALTER TABLE users ADD status VARCHAR(20) DEFAULT 'PENDING_ACTIVATION' NOT NULL;
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

UPDATE users SET status = 'DISABLED' WHERE disabled;
UPDATE users SET status = 'ACTIVE' WHERE NOT disabled;
UPDATE users SET display_name = username;

ALTER TABLE users ALTER COLUMN display_name SET NOT NULL;
ALTER TABLE users DROP COLUMN disabled;
ALTER TABLE users ADD CONSTRAINT users_password_hash_matches_status CHECK (((status = 'PENDING_ACTIVATION') AND (password_hash IS NULL)) OR ((status <> 'PENDING_ACTIVATION') AND (password_hash IS NOT NULL)))
