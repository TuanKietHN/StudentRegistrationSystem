-- Normalize courses schema to match current code expectations (course = course section).
-- This prevents legacy/manual schemas (with e-learning columns like credits/status) from breaking inserts.
-- Idempotent and safe to run on fresh DB created from V1 as well.

-- Drop legacy constraints that depend on legacy columns (if present)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_courses_status') THEN
        ALTER TABLE courses DROP CONSTRAINT chk_courses_status;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_courses_level') THEN
        ALTER TABLE courses DROP CONSTRAINT chk_courses_level;
    END IF;
END $$;

-- Drop legacy columns from courses (if present)
ALTER TABLE courses DROP COLUMN IF EXISTS description;
ALTER TABLE courses DROP COLUMN IF EXISTS thumbnail_url;
ALTER TABLE courses DROP COLUMN IF EXISTS status;
ALTER TABLE courses DROP COLUMN IF EXISTS credits;
ALTER TABLE courses DROP COLUMN IF EXISTS department_id;
ALTER TABLE courses DROP COLUMN IF EXISTS duration_weeks;
ALTER TABLE courses DROP COLUMN IF EXISTS level;
ALTER TABLE courses DROP COLUMN IF EXISTS language;

-- Drop legacy indexes (if present)
DROP INDEX IF EXISTS idx_courses_department_id;
DROP INDEX IF EXISTS idx_courses_status;
DROP INDEX IF EXISTS idx_courses_level;
DROP INDEX IF EXISTS idx_courses_enrollment_dates;

