CREATE TABLE IF NOT EXISTS messages (
    id uuid PRIMARY KEY,
    from_user_id uuid NOT NULL,
    to_user_id uuid NOT NULL,
    canvas_id uuid NOT NULL,
    message VARCHAR(100) NULL,
    show_name BOOLEAN NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    read_at TIMESTAMP NULL,
    CONSTRAINT fk_messages_from_user_id__id FOREIGN KEY (from_user_id)
        REFERENCES users(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_messages_to_user_id__id FOREIGN KEY (to_user_id)
        REFERENCES users(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_messages_canvas_id__id FOREIGN KEY (canvas_id)
        REFERENCES canvases(id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
