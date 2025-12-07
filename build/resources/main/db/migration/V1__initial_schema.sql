-- Q-Insight: Flyway Migration - Initial Schema.
-- This SQL script creates the initial database schema for the Ticketing system.
-- Flyway will execute this automatically on application startup.
-- The schema is designed based on the JPA entities defined in the domain layer.

-- Q-Insight: Table for Executives.
-- Stores information about service executives.
CREATE TABLE executives (
    id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    module VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    last_status_change TIMESTAMP
);

-- Q-Insight: Table for Tickets.
-- This is the central table, storing all tickets.
CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    ticket_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id VARCHAR(255) NOT NULL,
    telegram_chat_id VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    attention_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    attended_at TIMESTAMP,
    closed_at TIMESTAMP,
    executive_id UUID,
    FOREIGN KEY (executive_id) REFERENCES executives(id)
);

-- Q-Insight: Join Table for Executive Skills.
-- Maps which executives can handle which types of attention.
CREATE TABLE executive_attention_types (
    executive_id UUID NOT NULL,
    attention_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (executive_id, attention_type),
    FOREIGN KEY (executive_id) REFERENCES executives(id)
);

-- Q-Insight: Indexes for Performance.
-- Indexes are crucial for query performance, especially on frequently queried columns.
CREATE INDEX idx_tickets_status_attention_type ON tickets (status, attention_type);
CREATE INDEX idx_tickets_created_at ON tickets (created_at);
CREATE INDEX idx_executives_status ON executives (status);

-- Q-Insight: Initial Data Seeding.
-- This section seeds the database with initial data for testing and demonstration purposes.
-- In a real production environment, this might be handled differently.
INSERT INTO executives (id, full_name, module, status, last_status_change) VALUES
    (gen_random_uuid(), 'Alice Johnson', 'Modulo 1', 'AVAILABLE', NOW()),
    (gen_random_uuid(), 'Bob Williams', 'Modulo 2', 'AVAILABLE', NOW()),
    (gen_random_uuid(), 'Charlie Brown', 'Modulo 3', 'AVAILABLE', NOW());

-- Assigning skills to the seeded executives
-- Note: You'll need to fetch the UUIDs you just inserted. For simplicity, we assume we know them
-- or use a more advanced script. In a real scenario, use a deterministic UUID generation strategy
-- or a small application script to perform seeding after startup.

-- Let's assume the first UUID inserted was for Alice, second for Bob, third for Charlie.
-- This is NOT robust. For a real app, you'd use a different method.
DO $$
DECLARE
    alice_id UUID;
    bob_id UUID;
    charlie_id UUID;
BEGIN
    SELECT id INTO alice_id FROM executives WHERE full_name = 'Alice Johnson';
    SELECT id INTO bob_id FROM executives WHERE full_name = 'Bob Williams';
    SELECT id INTO charlie_id FROM executives WHERE full_name = 'Charlie Brown';

    INSERT INTO executive_attention_types (executive_id, attention_type) VALUES
        (alice_id, 'CAJA'),
        (alice_id, 'PERSONAL_BANKER'),
        (bob_id, 'PERSONAL_BANKER'),
        (bob_id, 'EMPRESAS'),
        (charlie_id, 'GERENCIA'),
        (charlie_id, 'CAJA');
END $$;
