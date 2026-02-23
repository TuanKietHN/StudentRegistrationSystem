-- =====================================================
-- V11__merge_subject_credit_columns.sql
-- Merge legacy subjects.credit (V1) into subjects.credits (V3) and drop credit.
-- Goal: Single source of truth is subjects.credits (used by JPA/domain).
-- =====================================================

DO $$
BEGIN
    -- Ensure the target column exists
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'subjects'
          AND column_name = 'credits'
    ) THEN
        ALTER TABLE subjects ADD COLUMN credits INTEGER;
    END IF;

    -- Move data from legacy credit -> credits (only when credits is null)
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'subjects'
          AND column_name = 'credit'
    ) THEN
        EXECUTE 'UPDATE subjects SET credits = COALESCE(credits, credit)';
    END IF;

    -- Enforce NOT NULL with a safe default
    EXECUTE 'UPDATE subjects SET credits = COALESCE(credits, 3)';

    ALTER TABLE subjects ALTER COLUMN credits SET DEFAULT 3;
    ALTER TABLE subjects ALTER COLUMN credits SET NOT NULL;

    -- Drop legacy column
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'subjects'
          AND column_name = 'credit'
    ) THEN
        ALTER TABLE subjects DROP COLUMN credit;
    END IF;
END $$;

