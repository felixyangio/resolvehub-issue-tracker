-- V1: Baseline schema for ResolveHub
-- This migration creates the initial tables if they don't already exist.
-- For existing databases, Flyway baseline should be run first.

CREATE TABLE IF NOT EXISTS users (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255)  NOT NULL,
    email       VARCHAR(255)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role        VARCHAR(50)   NOT NULL,
    enabled     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
);

CREATE TABLE IF NOT EXISTS incidents (
    id          UUID PRIMARY KEY,
    title       VARCHAR(200)  NOT NULL,
    description TEXT          NOT NULL,
    category    VARCHAR(50)   NOT NULL,
    priority    VARCHAR(50)   NOT NULL,
    status      VARCHAR(50)   NOT NULL,
    created_by  UUID          NOT NULL REFERENCES users(id),
    assigned_to UUID                   REFERENCES users(id),
    due_at      TIMESTAMP,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
);

CREATE TABLE IF NOT EXISTS comments (
    id          UUID PRIMARY KEY,
    incident_id UUID          NOT NULL REFERENCES incidents(id),
    author_id   UUID          NOT NULL REFERENCES users(id),
    content     TEXT          NOT NULL,
    author_role VARCHAR(50)   NOT NULL,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id          UUID PRIMARY KEY,
    incident_id UUID          NOT NULL REFERENCES incidents(id),
    actor_id    UUID          NOT NULL REFERENCES users(id),
    action      VARCHAR(50)   NOT NULL,
    old_value   VARCHAR(255),
    new_value   VARCHAR(255),
    message     TEXT,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
);
