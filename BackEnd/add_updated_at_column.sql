-- Add updated_at column to tables table
-- This script adds the missing updated_at column that the trigger expects

ALTER TABLE tables ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Update existing records to have current timestamp
UPDATE tables SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;