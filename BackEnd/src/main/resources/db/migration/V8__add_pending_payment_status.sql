-- Migration to add PENDING_PAYMENT status to orders table
-- This updates the status constraint to include the new PENDING_PAYMENT status

-- Step 1: Fix existing data - convert PAID status to PENDING_PAYMENT
UPDATE orders 
SET status = 'PENDING_PAYMENT' 
WHERE status = 'PAID';

-- Step 2: Drop existing constraint if it exists
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;

-- Step 3: Add new constraint with PENDING_PAYMENT included
ALTER TABLE orders 
ADD CONSTRAINT orders_status_check 
CHECK (status IN ('PLACED', 'CONFIRMED', 'PREPARING', 'READY', 'SERVED', 'PENDING_PAYMENT', 'CANCELLED'));

-- Add comment for documentation
COMMENT ON CONSTRAINT orders_status_check ON orders IS 'Valid order statuses including PENDING_PAYMENT for checkout';

