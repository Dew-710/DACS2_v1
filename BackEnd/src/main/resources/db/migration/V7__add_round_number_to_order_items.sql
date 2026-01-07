-- Add round_number and is_confirmed to track order rounds
-- round_number: Tracks which round of ordering this item belongs to (1st, 2nd, 3rd time calling waiter)
-- is_confirmed: FALSE = still in draft/temp state, TRUE = confirmed and included in final bill

ALTER TABLE order_items 
ADD COLUMN IF NOT EXISTS round_number INTEGER DEFAULT 1;

ALTER TABLE order_items 
ADD COLUMN IF NOT EXISTS is_confirmed BOOLEAN DEFAULT FALSE;

-- Add comment for documentation
COMMENT ON COLUMN order_items.round_number IS 'Lượt gọi món thứ mấy (1, 2, 3...)';
COMMENT ON COLUMN order_items.is_confirmed IS 'FALSE = đang draft, TRUE = đã confirm và tính tiền';

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_order_items_confirmed ON order_items(is_confirmed);
CREATE INDEX IF NOT EXISTS idx_order_items_round ON order_items(round_number);


