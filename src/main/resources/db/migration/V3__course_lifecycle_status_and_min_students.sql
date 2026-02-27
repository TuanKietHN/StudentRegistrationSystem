-- Add course lifecycle fields for university-style course registration workflow.
-- - status: OPEN/CLOSED/CANCELED/MERGED
-- - min_students: minimum enrollment threshold (for later merge/cancel decisions)
-- - canceled_at/reason: audit
-- - merged_into_course_id: link source section to target section when merged

ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'OPEN';

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_courses_lifecycle_status') THEN
        ALTER TABLE courses
            ADD CONSTRAINT chk_courses_lifecycle_status
            CHECK (status IN ('OPEN', 'CLOSED', 'CANCELED', 'MERGED'));
    END IF;
END $$;

ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS min_students INTEGER NOT NULL DEFAULT 0;

ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS canceled_at TIMESTAMP NULL;

ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS canceled_reason TEXT NULL;

ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS merged_into_course_id BIGINT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_type = 'FOREIGN KEY'
          AND table_name = 'courses'
          AND constraint_name = 'fk_courses_merged_into'
    ) THEN
        ALTER TABLE courses
            ADD CONSTRAINT fk_courses_merged_into
            FOREIGN KEY (merged_into_course_id) REFERENCES courses(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_courses_status ON courses(status);
CREATE INDEX IF NOT EXISTS idx_courses_merged_into ON courses(merged_into_course_id);

