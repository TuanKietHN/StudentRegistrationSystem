DO $$
DECLARE
    name_type text;
    code_type text;
BEGIN
    SELECT c.data_type
    INTO name_type
    FROM information_schema.columns c
    WHERE c.table_schema = 'public'
      AND c.table_name = 'sections'
      AND c.column_name = 'name';

    IF name_type = 'bytea' THEN
        EXECUTE 'ALTER TABLE public.sections ALTER COLUMN name TYPE text USING convert_from(name, ''UTF8'')';
    END IF;

    SELECT c.data_type
    INTO code_type
    FROM information_schema.columns c
    WHERE c.table_schema = 'public'
      AND c.table_name = 'sections'
      AND c.column_name = 'code';

    IF code_type = 'bytea' THEN
        EXECUTE 'ALTER TABLE public.sections ALTER COLUMN code TYPE text USING convert_from(code, ''UTF8'')';
    END IF;
END $$;

