-- V3: Add file attachments table for incident documents

CREATE TABLE IF NOT EXISTS attachments (
    id            UUID PRIMARY KEY,
    incident_id   UUID          NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    uploaded_by   UUID          NOT NULL REFERENCES users(id),
    original_name VARCHAR(255)  NOT NULL,
    stored_name   VARCHAR(255)  NOT NULL,
    content_type  VARCHAR(100)  NOT NULL,
    file_size     BIGINT        NOT NULL,
    created_at    TIMESTAMP     NOT NULL,
    updated_at    TIMESTAMP     NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_attachment_incident ON attachments (incident_id);
