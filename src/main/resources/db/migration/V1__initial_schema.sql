-- Initial Schema for Ticketero System

-- Workdays Table: Represents a business day.
CREATE TABLE workdays (
    id BIGSERIAL PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    status VARCHAR(255) NOT NULL
);

-- Executives Table: Represents service executives.
CREATE TABLE executives (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    module VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    last_status_change TIMESTAMP
);

-- Tickets Table: The central table for tickets.
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    codigo_referencia UUID NOT NULL UNIQUE,
    ticket_number VARCHAR(50) NOT NULL,
    national_id VARCHAR(255) NOT NULL,
    telefono VARCHAR(255),
    branch_office VARCHAR(255),
    position_in_queue INTEGER,
    estimated_wait_minutes INTEGER,
    assigned_module_number INTEGER,
    status VARCHAR(50) NOT NULL,
    attention_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    attended_at TIMESTAMP,
    closed_at TIMESTAMP,
    executive_id BIGINT,
    workday_id BIGINT NOT NULL,
    FOREIGN KEY (executive_id) REFERENCES executives(id),
    FOREIGN KEY (workday_id) REFERENCES workdays(id),
    CONSTRAINT uk_tickets_ticket_number_workday UNIQUE (ticket_number, workday_id)
);

-- Join Table for Executive Skills
CREATE TABLE executive_attention_types (
    executive_id BIGINT NOT NULL,
    attention_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (executive_id, attention_type),
    FOREIGN KEY (executive_id) REFERENCES executives(id)
);

-- Indexes for Performance
CREATE INDEX idx_tickets_status_attention_type ON tickets (status, attention_type);
CREATE INDEX idx_tickets_created_at ON tickets (created_at);
CREATE INDEX idx_executives_status ON executives (status);

-- Initial Data Seeding for demonstration
INSERT INTO executives (full_name, module, status, last_status_change) VALUES
    ('Alice Johnson', 'Modulo 1', 'AVAILABLE', NOW()),
    ('Bob Williams', 'Modulo 2', 'AVAILABLE', NOW()),
    ('Charlie Brown', 'Modulo 3', 'AVAILABLE', NOW());

DO $$
DECLARE
    alice_id BIGINT;
    bob_id BIGINT;
    charlie_id BIGINT;
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
