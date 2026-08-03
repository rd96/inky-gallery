ALTER TABLE drawings ADD CONSTRAINT check_drawings_0 CHECK (byte_size > 0);
ALTER TABLE drawings ADD CONSTRAINT check_drawings_1 CHECK ("position" >= 0);

ALTER TABLE drawings DROP CONSTRAINT check_byte_size_positive_non_zero;
ALTER TABLE drawings DROP CONSTRAINT check_position_positive;
