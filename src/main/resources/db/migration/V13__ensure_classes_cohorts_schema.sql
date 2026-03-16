DO $$
BEGIN
    IF to_regclass('public.classes') IS NULL AND to_regclass('public.subjects') IS NOT NULL THEN
        EXECUTE 'ALTER TABLE subjects RENAME TO classes';
    END IF;

    IF to_regclass('public.cohorts') IS NULL AND to_regclass('public.courses') IS NOT NULL THEN
        EXECUTE 'ALTER TABLE courses RENAME TO cohorts';
    END IF;

    IF to_regclass('public.cohort_time_slots') IS NULL AND to_regclass('public.course_time_slots') IS NOT NULL THEN
        EXECUTE 'ALTER TABLE course_time_slots RENAME TO cohort_time_slots';
    END IF;

    IF to_regclass('public.subjects_id_seq') IS NOT NULL AND to_regclass('public.classes_id_seq') IS NULL THEN
        EXECUTE 'ALTER SEQUENCE subjects_id_seq RENAME TO classes_id_seq';
        EXECUTE 'ALTER TABLE classes ALTER COLUMN id SET DEFAULT nextval(''classes_id_seq'')';
    END IF;

    IF to_regclass('public.courses_id_seq') IS NOT NULL AND to_regclass('public.cohorts_id_seq') IS NULL THEN
        EXECUTE 'ALTER SEQUENCE courses_id_seq RENAME TO cohorts_id_seq';
        EXECUTE 'ALTER TABLE cohorts ALTER COLUMN id SET DEFAULT nextval(''cohorts_id_seq'')';
    END IF;

    IF to_regclass('public.course_time_slots_id_seq') IS NOT NULL AND to_regclass('public.cohort_time_slots_id_seq') IS NULL THEN
        EXECUTE 'ALTER SEQUENCE course_time_slots_id_seq RENAME TO cohort_time_slots_id_seq';
        EXECUTE 'ALTER TABLE cohort_time_slots ALTER COLUMN id SET DEFAULT nextval(''cohort_time_slots_id_seq'')';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'cohorts' AND column_name = 'subject_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'cohorts' AND column_name = 'class_id'
    ) THEN
        EXECUTE 'ALTER TABLE cohorts RENAME COLUMN subject_id TO class_id';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'cohorts' AND column_name = 'merged_into_course_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'cohorts' AND column_name = 'merged_into_cohort_id'
    ) THEN
        EXECUTE 'ALTER TABLE cohorts RENAME COLUMN merged_into_course_id TO merged_into_cohort_id';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'enrollments' AND column_name = 'course_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'enrollments' AND column_name = 'cohort_id'
    ) THEN
        EXECUTE 'ALTER TABLE enrollments RENAME COLUMN course_id TO cohort_id';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'cohort_time_slots' AND column_name = 'course_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'cohort_time_slots' AND column_name = 'cohort_id'
    ) THEN
        EXECUTE 'ALTER TABLE cohort_time_slots RENAME COLUMN course_id TO cohort_id';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'attendance_sessions' AND column_name = 'course_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'attendance_sessions' AND column_name = 'cohort_id'
    ) THEN
        EXECUTE 'ALTER TABLE attendance_sessions RENAME COLUMN course_id TO cohort_id';
    END IF;

    IF to_regclass('public.v_courses_with_teacher') IS NOT NULL THEN
        EXECUTE 'DROP VIEW v_courses_with_teacher';
    END IF;

    EXECUTE '
        CREATE OR REPLACE VIEW v_cohorts_with_teacher AS
        SELECT
            c.id            AS cohort_id,
            c.code          AS cohort_code,
            c.name          AS cohort_name,
            c.semester_id,
            c.class_id,
            c.current_students,
            c.max_students,
            cl.name         AS class_name,
            sem.name        AS semester_name,
            t.id            AS teacher_id,
            t.user_id       AS teacher_user_id,
            u.username      AS teacher_username,
            u.email         AS teacher_email,
            t.employee_code,
            d.code          AS department_code,
            d.name          AS department_name
        FROM cohorts c
        LEFT JOIN classes cl    ON c.class_id = cl.id
        LEFT JOIN semesters sem ON c.semester_id = sem.id
        LEFT JOIN teachers t    ON c.teacher_id = t.id
        LEFT JOIN users u       ON t.user_id = u.id
        LEFT JOIN departments d ON t.department_id = d.id
    ';

    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_update_courses_updated_at') THEN
        EXECUTE 'ALTER TRIGGER trg_update_courses_updated_at ON cohorts RENAME TO trg_update_cohorts_updated_at';
    END IF;

    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_update_subjects_updated_at') THEN
        EXECUTE 'ALTER TRIGGER trg_update_subjects_updated_at ON classes RENAME TO trg_update_classes_updated_at';
    END IF;
END $$;

