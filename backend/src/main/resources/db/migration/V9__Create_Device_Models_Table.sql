CREATE TABLE IF NOT EXISTS device_model (
    id uuid PRIMARY KEY,
    device_name VARCHAR(50) NOT NULL,
    landscape_width_px INT NOT NULL,
    landscape_height_px INT NOT NULL,
    colour_swatch TEXT[] NULL
);

ALTER TABLE device_model ADD CONSTRAINT device_model_device_name_unique UNIQUE (device_name);