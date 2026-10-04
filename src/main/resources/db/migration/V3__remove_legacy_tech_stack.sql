-- The normalized technology data is now authoritative.
ALTER TABLE software_engineer
    DROP COLUMN IF EXISTS tech_stack;
