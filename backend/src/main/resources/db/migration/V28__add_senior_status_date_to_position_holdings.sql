-- Add senior_status_date column to position_holdings for tracking when judges take senior status
-- This is important for determining ACTIVE vs SENIOR judicial status

ALTER TABLE position_holdings
ADD COLUMN senior_status_date DATE NULL;

-- Add index for queries filtering by senior status
CREATE INDEX idx_holding_senior_status ON position_holdings(senior_status_date)
WHERE senior_status_date IS NOT NULL;

COMMENT ON COLUMN position_holdings.senior_status_date IS 'Date when judge assumed senior status (for FJC judicial data)';
