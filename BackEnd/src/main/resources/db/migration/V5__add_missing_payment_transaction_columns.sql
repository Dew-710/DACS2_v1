-- Flyway migration: Add missing columns to payment_transactions table

ALTER TABLE payment_transactions
ADD COLUMN IF NOT EXISTS payment_order_code BIGINT,
ADD COLUMN IF NOT EXISTS paid_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS reference VARCHAR(200);
