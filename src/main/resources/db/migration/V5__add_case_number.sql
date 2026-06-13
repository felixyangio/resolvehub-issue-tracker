-- V5: Add sequential case_number to incidents for human-readable IDs

CREATE SEQUENCE IF NOT EXISTS incident_case_number_seq START 1;

ALTER TABLE incidents ADD COLUMN IF NOT EXISTS case_number BIGINT;

-- Backfill existing rows in creation order
WITH numbered AS (
  SELECT id, ROW_NUMBER() OVER (ORDER BY created_at ASC) AS rn FROM incidents
)
UPDATE incidents SET case_number = numbered.rn
FROM numbered WHERE incidents.id = numbered.id;

-- Advance sequence past existing rows
DO $$
DECLARE max_num BIGINT;
BEGIN
  SELECT COALESCE(MAX(case_number), 0) INTO max_num FROM incidents;
  PERFORM setval('incident_case_number_seq', GREATEST(max_num + 1, 1), false);
END $$;

ALTER TABLE incidents ALTER COLUMN case_number SET NOT NULL;
ALTER TABLE incidents ALTER COLUMN case_number SET DEFAULT nextval('incident_case_number_seq');
ALTER TABLE incidents ADD CONSTRAINT uk_incident_case_number UNIQUE (case_number);
