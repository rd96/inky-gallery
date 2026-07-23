CREATE TABLE IF NOT EXISTS connections (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    recipient_id uuid NOT NULL,
    created_by uuid NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_connections_user_id__id FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_connections_recipient_id__id FOREIGN KEY (recipient_id)
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_connections_created_by__id FOREIGN KEY (created_by)
        REFERENCES users(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT no_self_connections CHECK (user_id <> recipient_id)
);

ALTER TABLE connections ADD CONSTRAINT connections_sender_recipient_unique UNIQUE (user_id, recipient_id);
