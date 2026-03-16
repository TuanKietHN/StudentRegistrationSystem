-- Fix drifted dev DB where admin_classes text columns were created as BYTEA (ddl-auto=update)
-- This migration is idempotent.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'admin_classes'
          AND column_name = 'code'
          AND data_type = 'bytea'
    ) THEN
        ALTER TABLE admin_classes
            ALTER COLUMN code TYPE VARCHAR(50)
            USING convert_from(code, 'UTF8');
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'admin_classes'
          AND column_name = 'name'
          AND data_type = 'bytea'
    ) THEN
        ALTER TABLE admin_classes
            ALTER COLUMN name TYPE VARCHAR(255)
            USING convert_from(name, 'UTF8');
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'admin_classes'
          AND column_name = 'program'
          AND data_type = 'bytea'
    ) THEN
        ALTER TABLE admin_classes
            ALTER COLUMN program TYPE VARCHAR(255)
            USING convert_from(program, 'UTF8');
    END IF;
END $$;

