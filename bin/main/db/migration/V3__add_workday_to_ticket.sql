ALTER TABLE tickets ADD COLUMN workday_id UUID;

ALTER TABLE tickets ADD CONSTRAINT fk_workday
    FOREIGN KEY (workday_id) REFERENCES workdays (id);

-- Optional: If you want to make workday_id NOT NULL, you would need to
-- backfill existing tickets with a default workday_id first.
-- For example:
-- UPDATE tickets SET workday_id = (SELECT id FROM workdays LIMIT 1) WHERE workday_id IS NULL;
-- ALTER TABLE tickets ALTER COLUMN workday_id SET NOT NULL;

-- Remove old unique constraint on ticket_number
ALTER TABLE tickets DROP CONSTRAINT IF EXISTS tickets_ticket_number_key;

-- Add new composite unique constraint
ALTER TABLE tickets ADD CONSTRAINT uk_tickets_ticket_number_workday UNIQUE (ticket_number, workday_id);
