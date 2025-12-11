-- Messages Table: Stores information about notifications to be sent.
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT,
    plantilla VARCHAR(50) NOT NULL,
    estado_envio VARCHAR(50) NOT NULL,
    fecha_programada TIMESTAMP NOT NULL,
    fecha_envio TIMESTAMP,
    telegram_message_id VARCHAR(255),
    intentos INTEGER NOT NULL DEFAULT 0,
    chat_id VARCHAR(255) NOT NULL,
    content TEXT,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id)
);
