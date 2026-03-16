-- Fix drifted dev DB where admin_classes text columns were created as BYTEA (ddl-auto=update)
-- Use pg_catalog for robust type detection.

DO $$
DECLARE
    coltype TEXT;
BEGIN
    IF to_regclass('admin_classes') IS NULL THEN
        RETURN;
    END IF;

    SELECT format_type(a.atttypid, a.atttypmod)
    INTO coltype
    FROM pg_attribute a
    JOIN pg_class c ON c.oid = a.attrelid
    JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = current_schema()
      AND c.relname = 'admin_classes'
      AND a.attname = 'code'
      AND a.attnum > 0
      AND NOT a.attisdropped;

    IF coltype = 'bytea' THEN
        EXECUTE 'ALTER TABLE admin_classes ALTER COLUMN code TYPE VARCHAR(50) USING convert_from(code, ''UTF8'')';
    END IF;

    SELECT format_type(a.atttypid, a.atttypmod)
    INTO coltype
    FROM pg_attribute a
    JOIN pg_class c ON c.oid = a.attrelid
    JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = current_schema()
      AND c.relname = 'admin_classes'
      AND a.attname = 'name'
      AND a.attnum > 0
      AND NOT a.attisdropped;

    IF coltype = 'bytea' THEN
        EXECUTE 'ALTER TABLE admin_classes ALTER COLUMN name TYPE VARCHAR(255) USING convert_from(name, ''UTF8'')';
    END IF;

    SELECT format_type(a.atttypid, a.atttypmod)
    INTO coltype
    FROM pg_attribute a
    JOIN pg_class c ON c.oid = a.attrelid
    JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = current_schema()
      AND c.relname = 'admin_classes'
      AND a.attname = 'program'
      AND a.attnum > 0
      AND NOT a.attisdropped;

    IF coltype = 'bytea' THEN
        EXECUTE 'ALTER TABLE admin_classes ALTER COLUMN program TYPE VARCHAR(255) USING convert_from(program, ''UTF8'')';
    END IF;
END $$;

