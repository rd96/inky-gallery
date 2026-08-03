CREATE TABLE IF NOT EXISTS canvases (
    id uuid PRIMARY KEY,
    target_device_id uuid NOT NULL,
    target_orientation VARCHAR(20) NOT NULL,
    canvas_status VARCHAR(20) NOT NULL,
    created_by uuid NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_canvases_target_device_id__id FOREIGN KEY (target_device_id)
        REFERENCES device_model(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_canvases_created_by__id FOREIGN KEY (created_by)
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE RESTRICT
);