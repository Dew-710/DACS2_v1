-- Flyway migration: Update payments method and status constraints

-- Step 1: Drop existing constraints if they exist
ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_method_check;
ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_status_check;

-- Step 2: Update invalid method values to a valid default (CASH)
-- This handles NULL, empty strings, or any other invalid values
UPDATE payments 
SET method = 'CASH' 
WHERE method IS NULL 
   OR method NOT IN ('CASH', 'CARD', 'DIGITAL_WALLET', 'PAYOS', 'SEPAY');

-- Step 3: Update invalid status values to a valid default (PENDING)
-- This handles NULL, empty strings, or any other invalid values
UPDATE payments 
SET status = 'PENDING' 
WHERE status IS NULL 
   OR status NOT IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED');

-- Step 4: Add new method constraint that allows CASH, CARD, DIGITAL_WALLET, PAYOS, and SEPAY
ALTER TABLE payments 
ADD CONSTRAINT payments_method_check 
CHECK (method IN ('CASH', 'CARD', 'DIGITAL_WALLET', 'PAYOS', 'SEPAY'));

-- Step 5: Add new status constraint that allows PENDING, COMPLETED, FAILED, REFUNDED, and CANCELLED
ALTER TABLE payments 
ADD CONSTRAINT payments_status_check 
CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED'));



