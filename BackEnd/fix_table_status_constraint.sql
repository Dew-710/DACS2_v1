-- Fix table status constraint to use 'AVAILABLE' instead of 'VACANT'
-- This script updates the check constraint and converts existing 'VACANT' statuses to 'AVAILABLE'

-- First, drop the existing constraint
ALTER TABLE tables DROP CONSTRAINT IF EXISTS tables_status_check;

-- Update any existing 'VACANT' statuses to 'AVAILABLE'
UPDATE tables SET status = 'AVAILABLE' WHERE status = 'VACANT';

-- Add the new constraint with 'AVAILABLE' instead of 'VACANT'
ALTER TABLE tables ADD CONSTRAINT tables_status_check
    CHECK (status IN ('AVAILABLE', 'RESERVED', 'OCCUPIED', 'CLEANING', 'MAINTENANCE'));