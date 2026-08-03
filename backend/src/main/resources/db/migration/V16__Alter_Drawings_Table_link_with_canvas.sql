-- this migration will fail if you have any drawings in the drawings table
-- run this to delete the drawings and proceed
-- DELETE FROM drawings;

ALTER TABLE drawings
    ADD canvas_id uuid NOT NULL,
    ADD "position" INT NOT NULL;

ALTER TABLE drawings
    DROP CONSTRAINT check_drawings_0,
    DROP CONSTRAINT check_drawings_1,
    DROP CONSTRAINT check_drawings_2;

ALTER TABLE drawings
    ADD CONSTRAINT unique_canvas_id_position UNIQUE (canvas_id, "position"),
    ADD CONSTRAINT fk_drawings_canvas_id__id FOREIGN KEY (canvas_id)
        REFERENCES canvases(id) ON DELETE CASCADE ON UPDATE RESTRICT;

ALTER TABLE drawings
    DROP COLUMN user_id,
    DROP COLUMN width_px,
    DROP COLUMN height_px;

ALTER TABLE drawings
    ADD CONSTRAINT check_byte_size_positive_non_zero CHECK (byte_size > 0),
    ADD CONSTRAINT check_position_positive CHECK (position >= 0);
