-- V23__alter_statutes_source_credit.sql
-- Increase source_credit column size - legislative history citations can be very long

ALTER TABLE statutes ALTER COLUMN source_credit TYPE TEXT;

COMMENT ON COLUMN statutes.source_credit IS 'Legislative history citation (can be lengthy for frequently amended sections)';
