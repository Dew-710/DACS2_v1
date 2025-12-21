-- Generate QR codes for all tables that don't have one
-- This script updates all tables with unique QR codes

-- Update existing tables with QR codes
UPDATE tables SET qr_code = 'TABLE-' || UPPER(SUBSTRING(MD5(RANDOM()::text || id::text) FROM 1 FOR 8))
WHERE qr_code IS NULL;

-- Ensure uniqueness (in case of collisions, run again)
-- This is a simple approach, in production you might want more robust uniqueness checking
