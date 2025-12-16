-- V13__add_appointment_type_to_position.sql
-- FB-2.2: Add appointment type field for executive positions

ALTER TABLE government_positions
ADD COLUMN appointment_type VARCHAR(10);

CREATE INDEX idx_position_appointment_type ON government_positions(appointment_type);

COMMENT ON COLUMN government_positions.appointment_type IS
    'PLUM appointment type: PAS, PA, NA, CA, XS';
