DO $$
BEGIN
    IF to_regclass('public.subjects') IS NULL AND to_regclass('public.classes') IS NOT NULL THEN
        EXECUTE 'ALTER TABLE classes RENAME TO subjects';
    END IF;

    IF to_regclass('public.classes_id_seq') IS NOT NULL AND to_regclass('public.subjects_id_seq') IS NULL THEN
        EXECUTE 'ALTER SEQUENCE classes_id_seq RENAME TO subjects_id_seq';
        EXECUTE 'ALTER TABLE subjects ALTER COLUMN id SET DEFAULT nextval(''subjects_id_seq'')';
    END IF;

    IF to_regclass('public.role_permissions') IS NOT NULL AND to_regclass('public.permissions') IS NOT NULL THEN
        EXECUTE '
            DELETE FROM role_permissions rp
            USING permissions p
            WHERE rp.permission_id = p.id
              AND p.name LIKE ''CLASS:%''
        ';

        EXECUTE '
            DELETE FROM permissions
            WHERE name LIKE ''CLASS:%''
        ';
    END IF;
END $$;

