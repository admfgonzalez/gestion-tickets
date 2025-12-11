-- Audit Logs Table: Stores a trail of all important events in the system.
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    actor VARCHAR(255),
    entity_type VARCHAR(255),
    entity_id BIGINT,
    details TEXT
);
