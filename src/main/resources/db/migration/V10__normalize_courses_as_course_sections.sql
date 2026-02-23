-- =====================================================
-- V10__normalize_courses_as_course_sections.sql
-- Scope decision: Keep "courses" as V1 lớp học phần (course section)
-- Removes e-learning oriented columns introduced in V3.
-- Also fixes the v_courses_with_teacher view to treat teacher_id as users.id
-- (teacher is an optional profile of user).
-- =====================================================

-- 1) Drop constraints that depend on V3 columns (if present)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_courses_status'
    ) THEN
        ALTER TABLE courses DROP CONSTRAINT chk_courses_status;
    END IF;

    IF EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_courses_level'
    ) THEN
        ALTER TABLE courses DROP CONSTRAINT chk_courses_level;
    END IF;
END $$;

-- 2) Drop V3 columns from courses (idempotent)
ALTER TABLE courses DROP COLUMN IF EXISTS description;
ALTER TABLE courses DROP COLUMN IF EXISTS thumbnail_url;
ALTER TABLE courses DROP COLUMN IF EXISTS status;
ALTER TABLE courses DROP COLUMN IF EXISTS credits;
ALTER TABLE courses DROP COLUMN IF EXISTS department_id;
ALTER TABLE courses DROP COLUMN IF EXISTS duration_weeks;
ALTER TABLE courses DROP COLUMN IF EXISTS level;
ALTER TABLE courses DROP COLUMN IF EXISTS language;
ALTER TABLE courses DROP COLUMN IF EXISTS enrollment_start_date;
ALTER TABLE courses DROP COLUMN IF EXISTS enrollment_end_date;

-- 3) Remove V3 indexes related to dropped columns (if present)
DROP INDEX IF EXISTS idx_courses_department_id;
DROP INDEX IF EXISTS idx_courses_status;
DROP INDEX IF EXISTS idx_courses_level;
DROP INDEX IF EXISTS idx_courses_enrollment_dates;

-- 4) Ensure teacher_id FK points to users(id) (teacher is a user)
DO $$
DECLARE
    fk_target text;
BEGIN
    SELECT ccu.table_name
    INTO fk_target
    FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu
        ON tc.constraint_name = kcu.constraint_name
       AND tc.table_schema = kcu.table_schema
    JOIN information_schema.constraint_column_usage ccu
        ON ccu.constraint_name = tc.constraint_name
       AND ccu.table_schema = tc.table_schema
    WHERE tc.constraint_type = 'FOREIGN KEY'
      AND tc.table_name = 'courses'
      AND tc.constraint_name = 'fk_courses_teacher';

    IF fk_target IS NOT NULL AND fk_target <> 'users' THEN
        ALTER TABLE courses DROP CONSTRAINT fk_courses_teacher;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_type = 'FOREIGN KEY'
          AND table_name = 'courses'
          AND constraint_name = 'fk_courses_teacher'
    ) THEN
        ALTER TABLE courses
            ADD CONSTRAINT fk_courses_teacher
            FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END $$;

-- 5) Fix the view to treat teacher as optional profile of user
CREATE OR REPLACE VIEW v_courses_with_teacher AS
SELECT
    c.id AS course_id,
    c.code AS course_code,
    c.name AS course_name,
    c.semester_id,
    c.subject_id,
    c.current_students,
    c.max_students,
    s.name AS subject_name,
    sem.name AS semester_name,
    u.id AS teacher_user_id,
    u.username AS teacher_username,
    u.email AS teacher_email,
    t.id AS teacher_profile_id,
    t.employee_code,
    d.code AS department_code,
    d.name AS department_name
FROM courses c
LEFT JOIN subjects s ON c.subject_id = s.id
LEFT JOIN semesters sem ON c.semester_id = sem.id
LEFT JOIN users u ON c.teacher_id = u.id
LEFT JOIN teachers t ON t.user_id = u.id
LEFT JOIN departments d ON t.department_id = d.id;
