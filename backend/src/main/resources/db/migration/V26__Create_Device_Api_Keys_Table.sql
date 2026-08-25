CREATE TABLE IF NOT EXISTS device_api_keys (
    id uuid PRIMARY KEY,
    "deviceId" uuid NOT NULL,
    key_hash VARCHAR(64) NOT NULL,
    api_key_reference VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    CONSTRAINT fk_device_api_keys_deviceid__id FOREIGN KEY ("deviceId")
        REFERENCES devices(id) ON DELETE CASCADE ON UPDATE RESTRICT
);

ALTER TABLE device_api_keys ADD CONSTRAINT device_api_keys_key_hash_unique UNIQUE (key_hash);