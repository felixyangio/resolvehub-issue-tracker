-- V2: Add indexes for query performance + version column for optimistic locking

-- === Single-column indexes ===
-- Used by: CaseList status filter, Dashboard status counts
CREATE INDEX IF NOT EXISTS idx_incident_status      ON incidents (status);

-- Used by: Dashboard priority distribution
CREATE INDEX IF NOT EXISTS idx_incident_priority    ON incidents (priority);

-- Used by: Dashboard category pie chart
CREATE INDEX IF NOT EXISTS idx_incident_category    ON incidents (category);

-- Used by: findAll with createdBy specification (USER role)
CREATE INDEX IF NOT EXISTS idx_incident_created_by  ON incidents (created_by);

-- Used by: findAll with assignedTo specification (AGENT role)
CREATE INDEX IF NOT EXISTS idx_incident_assigned_to ON incidents (assigned_to);

-- Used by: weekly trend queries (countCreatedBetween)
CREATE INDEX IF NOT EXISTS idx_incident_created_at  ON incidents (created_at);

-- === Composite indexes (leftmost-prefix principle) ===
-- Used by: countByCreatedByIdAndStatus (USER dashboard)
CREATE INDEX IF NOT EXISTS idx_incident_created_by_status  ON incidents (created_by, status);

-- Used by: countByAssignedToIdAndStatus (AGENT dashboard)
CREATE INDEX IF NOT EXISTS idx_incident_assigned_to_status ON incidents (assigned_to, status);

-- === Optimistic locking ===
-- Add version column to prevent lost updates when concurrent users modify the same incident
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
