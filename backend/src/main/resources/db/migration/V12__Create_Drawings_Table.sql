CREATE TABLE IF NOT EXISTS drawings (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    width_px INT NOT NULL,
    height_px INT NOT NULL,
    byte_size INT NOT NULL,
    png_data bytea NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_drawings_user_id__id FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT check_drawings_0 CHECK (width_px BETWEEN 100 AND 10000),
    CONSTRAINT check_drawings_1 CHECK (height_px BETWEEN 100 AND 10000),
    CONSTRAINT check_drawings_2 CHECK (byte_size > 0)
);
