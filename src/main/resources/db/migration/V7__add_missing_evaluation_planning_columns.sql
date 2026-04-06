-- Add missing columns to evaluation table
ALTER TABLE evaluation ADD COLUMN score INT DEFAULT NULL;

-- Add missing columns to planning table
ALTER TABLE planning ADD COLUMN date_debut DATE DEFAULT NULL;
ALTER TABLE planning ADD COLUMN date_fin DATE DEFAULT NULL;
ALTER TABLE planning ADD COLUMN type VARCHAR(100) DEFAULT NULL;
