DO $$
DECLARE
    col_type text;
BEGIN
    SELECT c.data_type
    INTO col_type
    FROM information_schema.columns c
    WHERE c.table_schema = 'public'
      AND c.table_name = 'teachers'
      AND c.column_name = 'employee_code';
    IF col_type = 'bytea' THEN
        EXECUTE 'ALTER TABLE public.teachers ALTER COLUMN employee_code TYPE varchar(50) USING convert_from(employee_code, ''UTF8'')';
    END IF;

    SELECT c.data_type
    INTO col_type
    FROM information_schema.columns c
    WHERE c.table_schema = 'public'
      AND c.table_name = 'cohorts'
      AND c.column_name = 'code';
    IF col_type = 'bytea' THEN
        EXECUTE 'ALTER TABLE public.cohorts ALTER COLUMN code TYPE text USING convert_from(code, ''UTF8'')';
    END IF;

    SELECT c.data_type
    INTO col_type
    FROM information_schema.columns c
    WHERE c.table_schema = 'public'
      AND c.table_name = 'cohorts'
      AND c.column_name = 'name';
    IF col_type = 'bytea' THEN
        EXECUTE 'ALTER TABLE public.cohorts ALTER COLUMN name TYPE text USING convert_from(name, ''UTF8'')';
    END IF;

    SELECT c.data_type
    INTO col_type
    FROM information_schema.columns c
    WHERE c.table_schema = 'public'
      AND c.table_name = 'student_classes'
      AND c.column_name = 'code';
    IF col_type = 'bytea' THEN
        EXECUTE 'ALTER TABLE public.student_classes ALTER COLUMN code TYPE text USING convert_from(code, ''UTF8'')';
    END IF;

    SELECT c.data_type
    INTO col_type
    FROM information_schema.columns c
    WHERE c.table_schema = 'public'
      AND c.table_name = 'student_classes'
      AND c.column_name = 'name';
    IF col_type = 'bytea' THEN
        EXECUTE 'ALTER TABLE public.student_classes ALTER COLUMN name TYPE text USING convert_from(name, ''UTF8'')';
    END IF;
END $$;

