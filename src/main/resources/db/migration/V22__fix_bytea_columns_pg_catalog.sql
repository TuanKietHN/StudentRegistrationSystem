DO $$
DECLARE
    typ text;
BEGIN
    SELECT t.typname
    INTO typ
    FROM pg_catalog.pg_attribute a
    JOIN pg_catalog.pg_class c ON c.oid = a.attrelid
    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
    JOIN pg_catalog.pg_type t ON t.oid = a.atttypid
    WHERE n.nspname = 'public'
      AND c.relname = 'teachers'
      AND a.attname = 'employee_code'
      AND a.attnum > 0
      AND NOT a.attisdropped;
    IF typ = 'bytea' THEN
        EXECUTE 'ALTER TABLE public.teachers ALTER COLUMN employee_code TYPE varchar(50) USING convert_from(employee_code, ''UTF8'')';
    END IF;

    SELECT t.typname
    INTO typ
    FROM pg_catalog.pg_attribute a
    JOIN pg_catalog.pg_class c ON c.oid = a.attrelid
    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
    JOIN pg_catalog.pg_type t ON t.oid = a.atttypid
    WHERE n.nspname = 'public'
      AND c.relname = 'cohorts'
      AND a.attname = 'code'
      AND a.attnum > 0
      AND NOT a.attisdropped;
    IF typ = 'bytea' THEN
        EXECUTE 'ALTER TABLE public.cohorts ALTER COLUMN code TYPE text USING convert_from(code, ''UTF8'')';
    END IF;

    SELECT t.typname
    INTO typ
    FROM pg_catalog.pg_attribute a
    JOIN pg_catalog.pg_class c ON c.oid = a.attrelid
    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
    JOIN pg_catalog.pg_type t ON t.oid = a.atttypid
    WHERE n.nspname = 'public'
      AND c.relname = 'cohorts'
      AND a.attname = 'name'
      AND a.attnum > 0
      AND NOT a.attisdropped;
    IF typ = 'bytea' THEN
        EXECUTE 'ALTER TABLE public.cohorts ALTER COLUMN name TYPE text USING convert_from(name, ''UTF8'')';
    END IF;

    SELECT t.typname
    INTO typ
    FROM pg_catalog.pg_attribute a
    JOIN pg_catalog.pg_class c ON c.oid = a.attrelid
    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
    JOIN pg_catalog.pg_type t ON t.oid = a.atttypid
    WHERE n.nspname = 'public'
      AND c.relname = 'departments'
      AND a.attname = 'code'
      AND a.attnum > 0
      AND NOT a.attisdropped;
    IF typ = 'bytea' THEN
        EXECUTE 'ALTER TABLE public.departments ALTER COLUMN code TYPE varchar(20) USING convert_from(code, ''UTF8'')';
    END IF;

    SELECT t.typname
    INTO typ
    FROM pg_catalog.pg_attribute a
    JOIN pg_catalog.pg_class c ON c.oid = a.attrelid
    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
    JOIN pg_catalog.pg_type t ON t.oid = a.atttypid
    WHERE n.nspname = 'public'
      AND c.relname = 'departments'
      AND a.attname = 'name'
      AND a.attnum > 0
      AND NOT a.attisdropped;
    IF typ = 'bytea' THEN
        EXECUTE 'ALTER TABLE public.departments ALTER COLUMN name TYPE varchar(255) USING convert_from(name, ''UTF8'')';
    END IF;

    SELECT t.typname
    INTO typ
    FROM pg_catalog.pg_attribute a
    JOIN pg_catalog.pg_class c ON c.oid = a.attrelid
    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
    JOIN pg_catalog.pg_type t ON t.oid = a.atttypid
    WHERE n.nspname = 'public'
      AND c.relname = 'subjects'
      AND a.attname = 'code'
      AND a.attnum > 0
      AND NOT a.attisdropped;
    IF typ = 'bytea' THEN
        EXECUTE 'ALTER TABLE public.subjects ALTER COLUMN code TYPE varchar(255) USING convert_from(code, ''UTF8'')';
    END IF;

    SELECT t.typname
    INTO typ
    FROM pg_catalog.pg_attribute a
    JOIN pg_catalog.pg_class c ON c.oid = a.attrelid
    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
    JOIN pg_catalog.pg_type t ON t.oid = a.atttypid
    WHERE n.nspname = 'public'
      AND c.relname = 'subjects'
      AND a.attname = 'name'
      AND a.attnum > 0
      AND NOT a.attisdropped;
    IF typ = 'bytea' THEN
        EXECUTE 'ALTER TABLE public.subjects ALTER COLUMN name TYPE varchar(255) USING convert_from(name, ''UTF8'')';
    END IF;
END $$;

