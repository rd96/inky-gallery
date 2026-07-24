CREATE TABLE IF NOT EXISTS devices (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    device_model_id uuid NOT NULL,
    device_nickname VARCHAR(50) NOT NULL,
    orientation VARCHAR(20) DEFAULT 'PORTRAIT' NOT NULL,
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT fk_devices_user_id__id FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_devices_device_model_id__id FOREIGN KEY (device_model_id)
        REFERENCES device_model(id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE INDEX devices_user_id ON devices (user_id);