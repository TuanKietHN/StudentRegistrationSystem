DO $$
BEGIN
    IF to_regclass('public.subjects') IS NULL AND to_regclass('public.classes') IS NOT NULL THEN
        EXECUTE 'ALTER TABLE classes RENAME TO subjects';
    END IF;

    IF to_regclass('public.sections') IS NULL AND to_regclass('public.cohorts') IS NOT NULL THEN
        EXECUTE 'ALTER TABLE cohorts RENAME TO sections';
    END IF;

    IF to_regclass('public.section_time_slots') IS NULL AND to_regclass('public.cohort_time_slots') IS NOT NULL THEN
        EXECUTE 'ALTER TABLE cohort_time_slots RENAME TO section_time_slots';
    END IF;

    IF to_regclass('public.student_classes') IS NULL AND to_regclass('public.admin_classes') IS NOT NULL THEN
        EXECUTE 'ALTER TABLE admin_classes RENAME TO student_classes';
    END IF;

    IF to_regclass('public.classes_id_seq') IS NOT NULL AND to_regclass('public.subjects_id_seq') IS NULL THEN
        EXECUTE 'ALTER SEQUENCE classes_id_seq RENAME TO subjects_id_seq';
        EXECUTE 'ALTER TABLE subjects ALTER COLUMN id SET DEFAULT nextval(''subjects_id_seq'')';
    END IF;

    IF to_regclass('public.cohorts_id_seq') IS NOT NULL AND to_regclass('public.sections_id_seq') IS NULL THEN
        EXECUTE 'ALTER SEQUENCE cohorts_id_seq RENAME TO sections_id_seq';
        EXECUTE 'ALTER TABLE sections ALTER COLUMN id SET DEFAULT nextval(''sections_id_seq'')';
    END IF;

    IF to_regclass('public.cohort_time_slots_id_seq') IS NOT NULL AND to_regclass('public.section_time_slots_id_seq') IS NULL THEN
        EXECUTE 'ALTER SEQUENCE cohort_time_slots_id_seq RENAME TO section_time_slots_id_seq';
        EXECUTE 'ALTER TABLE section_time_slots ALTER COLUMN id SET DEFAULT nextval(''section_time_slots_id_seq'')';
    END IF;

    IF to_regclass('public.admin_classes_id_seq') IS NOT NULL AND to_regclass('public.student_classes_id_seq') IS NULL THEN
        EXECUTE 'ALTER SEQUENCE admin_classes_id_seq RENAME TO student_classes_id_seq';
        EXECUTE 'ALTER TABLE student_classes ALTER COLUMN id SET DEFAULT nextval(''student_classes_id_seq'')';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'sections' AND column_name = 'class_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'sections' AND column_name = 'subject_id'
    ) THEN
        EXECUTE 'ALTER TABLE sections RENAME COLUMN class_id TO subject_id';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'sections' AND column_name = 'merged_into_cohort_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'sections' AND column_name = 'merged_into_section_id'
    ) THEN
        EXECUTE 'ALTER TABLE sections RENAME COLUMN merged_into_cohort_id TO merged_into_section_id';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'enrollments' AND column_name = 'cohort_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'enrollments' AND column_name = 'section_id'
    ) THEN
        EXECUTE 'ALTER TABLE enrollments RENAME COLUMN cohort_id TO section_id';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'attendance_sessions' AND column_name = 'cohort_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'attendance_sessions' AND column_name = 'section_id'
    ) THEN
        EXECUTE 'ALTER TABLE attendance_sessions RENAME COLUMN cohort_id TO section_id';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'section_time_slots' AND column_name = 'cohort_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'section_time_slots' AND column_name = 'section_id'
    ) THEN
        EXECUTE 'ALTER TABLE section_time_slots RENAME COLUMN cohort_id TO section_id';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'students' AND column_name = 'admin_class_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'students' AND column_name = 'student_class_id'
    ) THEN
        EXECUTE 'ALTER TABLE students RENAME COLUMN admin_class_id TO student_class_id';
    END IF;

    IF to_regclass('public.v_courses_with_teacher') IS NOT NULL THEN
        EXECUTE 'DROP VIEW v_courses_with_teacher';
    END IF;
    IF to_regclass('public.v_cohorts_with_teacher') IS NOT NULL THEN
        EXECUTE 'DROP VIEW v_cohorts_with_teacher';
    END IF;

    IF to_regclass('public.v_sections_with_teacher') IS NULL AND to_regclass('public.sections') IS NOT NULL THEN
        EXECUTE '
            CREATE VIEW v_sections_with_teacher AS
            SELECT
                s.id            AS section_id,
                s.code          AS section_code,
                s.name          AS section_name,
                s.semester_id,
                s.subject_id,
                s.current_students,
                s.max_students,
                subj.name       AS subject_name,
                sem.name        AS semester_name,
                t.id            AS teacher_id,
                t.user_id       AS teacher_user_id,
                u.username      AS teacher_username,
                u.email         AS teacher_email,
                t.employee_code,
                d.code          AS department_code,
                d.name          AS department_name
            FROM sections s
            LEFT JOIN subjects subj ON s.subject_id = subj.id
            LEFT JOIN semesters sem ON s.semester_id = sem.id
            LEFT JOIN teachers t    ON s.teacher_id = t.id
            LEFT JOIN users u       ON t.user_id = u.id
            LEFT JOIN departments d ON t.department_id = d.id
        ';
    END IF;

    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_update_cohorts_updated_at') THEN
        EXECUTE 'ALTER TRIGGER trg_update_cohorts_updated_at ON sections RENAME TO trg_update_sections_updated_at';
    END IF;

    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_update_classes_updated_at') THEN
        EXECUTE 'ALTER TRIGGER trg_update_classes_updated_at ON subjects RENAME TO trg_update_subjects_updated_at';
    END IF;

    IF to_regclass('public.cohorts') IS NULL THEN
        EXECUTE '
            CREATE TABLE cohorts (
                id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                code VARCHAR(50) NOT NULL UNIQUE,
                name VARCHAR(255) NOT NULL,
                start_year INT NOT NULL,
                end_year INT NOT NULL,
                active BOOLEAN NOT NULL DEFAULT TRUE
            )
        ';
        EXECUTE 'CREATE TRIGGER trg_update_cohorts_updated_at BEFORE UPDATE ON cohorts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column()';
    END IF;

    IF to_regclass('public.student_classes') IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'student_classes' AND column_name = 'cohort_id'
    ) THEN
        EXECUTE 'ALTER TABLE student_classes ADD COLUMN cohort_id BIGINT';
        EXECUTE 'ALTER TABLE student_classes ADD CONSTRAINT fk_student_classes_cohort FOREIGN KEY (cohort_id) REFERENCES cohorts(id)';
    END IF;

    IF to_regclass('public.student_classes') IS NOT NULL AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'student_classes' AND column_name = 'intake_year'
    ) THEN
        EXECUTE '
            INSERT INTO cohorts(code, name, start_year, end_year, active)
            SELECT
                ''COHORT_'' || sc.intake_year AS code,
                ''Niên khóa '' || sc.intake_year AS name,
                sc.intake_year AS start_year,
                sc.intake_year + 4 AS end_year,
                TRUE
            FROM (SELECT DISTINCT intake_year FROM student_classes WHERE intake_year IS NOT NULL) sc
            WHERE NOT EXISTS (
                SELECT 1 FROM cohorts c WHERE c.code = ''COHORT_'' || sc.intake_year
            )
        ';
        EXECUTE '
            UPDATE student_classes s
            SET cohort_id = c.id
            FROM cohorts c
            WHERE s.cohort_id IS NULL
              AND s.intake_year IS NOT NULL
              AND c.code = ''COHORT_'' || s.intake_year
        ';
    END IF;
END $$;

