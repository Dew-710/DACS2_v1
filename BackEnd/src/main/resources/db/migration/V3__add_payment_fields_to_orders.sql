-- Flyway migration: add payment fields to orders table for PayOS integration

ALTER TABLE orders
ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50),
ADD COLUMN IF NOT EXISTS checkout_url TEXT,
ADD COLUMN IF NOT EXISTS qr_code TEXT;

-- Add index for better performance if needed
CREATE INDEX IF NOT EXISTS idx_orders_payment_status ON orders(payment_status);
CREATE INDEX IF NOT EXISTS idx_orders_checkout_url ON orders(checkout_url);
CREATE INDEX IF NOT EXISTS idx_orders_qr_code ON orders(qr_code);
