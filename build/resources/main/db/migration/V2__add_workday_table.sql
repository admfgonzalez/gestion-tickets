CREATE TABLE workdays (
    id UUID PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    status VARCHAR(255) NOT NULL
);
