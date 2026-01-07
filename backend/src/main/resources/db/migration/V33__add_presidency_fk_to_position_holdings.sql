-- V33__add_presidency_fk_to_position_holdings.sql
-- KB-1.1: Add FK constraint from position_holdings.presidency_id to presidencies.id
-- Note: The presidency_id column was added in V30, FK deferred until presidencies table exists

-- =====================================================================
-- 1. Add foreign key constraint
-- =====================================================================
ALTER TABLE position_holdings
    ADD CONSTRAINT fk_position_holdings_presidency
    FOREIGN KEY (presidency_id) REFERENCES presidencies(id);

COMMENT ON CONSTRAINT fk_position_holdings_presidency ON position_holdings IS
    'Links executive branch appointments (VP, Cabinet, CoS) to specific presidencies';
