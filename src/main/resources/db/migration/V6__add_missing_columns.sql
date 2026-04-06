-- ============================================================
-- V6 — Add missing columns to align Entity definitions
--                    with database schema
-- ============================================================

-- Add 'organisme' column to formation table (maps to Formation.organisme)
ALTER TABLE formation ADD COLUMN organisme VARCHAR(200) NULL AFTER certification;
