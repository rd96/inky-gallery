ALTER TABLE users ADD activation_status VARCHAR(20) DEFAULT 'PENDING' NOT NULL;

ALTER TABLE users ADD enabled BOOLEAN DEFAULT TRUE NOT NULL;

UPDATE users SET enabled = false WHERE status = 'DISABLED';
UPDATE users SET activation_status = 'ACTIVATED' WHERE status = 'ACTIVE';

ALTER TABLE users DROP CONSTRAINT users_password_hash_matches_status;
ALTER TABLE users DROP COLUMN status;

ALTER TABLE users ADD CONSTRAINT users_password_hash_matches_activation_status CHECK (((activation_status = 'PENDING') AND (password_hash IS NULL)) OR ((activation_status = 'ACTIVATED') AND (password_hash IS NOT NULL)));

