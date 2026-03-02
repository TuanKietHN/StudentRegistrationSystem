-- Ensure grade weight columns exist for subjects (process/exam)
-- This migration is intentionally idempotent to fix drifted dev databases.

ALTER TABLE subjects
    ADD COLUMN IF NOT EXISTS process_weight SMALLINT,
    ADD COLUMN IF NOT EXISTS exam_weight SMALLINT;

UPDATE subjects
SET process_weight = 40
WHERE process_weight IS NULL;

UPDATE subjects
SET exam_weight = 60
WHERE exam_weight IS NULL;

ALTER TABLE subjects
    ALTER COLUMN process_weight SET DEFAULT 40,
    ALTER COLUMN exam_weight SET DEFAULT 60,
    ALTER COLUMN process_weight SET NOT NULL,
    ALTER COLUMN exam_weight SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_subjects_grade_weights'
    ) THEN
        ALTER TABLE subjects
            ADD CONSTRAINT chk_subjects_grade_weights
            CHECK (
                process_weight BETWEEN 0 AND 100
                AND exam_weight BETWEEN 0 AND 100
                AND (process_weight + exam_weight = 100)
            );
    END IF;
END $$;

