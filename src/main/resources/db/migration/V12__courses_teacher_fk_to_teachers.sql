       -- =====================================================
-- V12__courses_teacher_fk_to_teachers.sql
-- Enforce courses.teacher_id -> teachers.id (database-level constraint)
-- Data migration:
--   - courses.teacher_id previously stored users.id
--   - create missing teachers profiles (teachers.user_id) for referenced users
--   - rewrite courses.teacher_id to teachers.id
-- =====================================================

-- 1) Temporarily keep the current teacher user id values
ALTER TABLE courses ADD COLUMN IF NOT EXISTS teacher_user_id_tmp BIGINT;

UPDATE courses
SET teacher_user_id_tmp = teacher_id
WHERE teacher_id IS NOT NULL
  AND teacher_user_id_tmp IS NULL;

-- 2) Ensure teachers profiles exist for:
--    - any user referenced by courses.teacher_user_id_tmp
--    - any user having ROLE_TEACHER
INSERT INTO teachers (user_id, employee_code, active, created_at, updated_at)
SELECT
    u.id,
    ('GV' || LPAD(u.id::text, 6, '0')) AS employee_code,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u
WHERE (
        u.id IN (SELECT DISTINCT teacher_user_id_tmp FROM courses WHERE teacher_user_id_tmp IS NOT NULL)
        OR EXISTS (
            SELECT 1
            FROM user_roles ur
            JOIN roles r ON r.id = ur.role_id
            WHERE ur.user_id = u.id
              AND r.name = 'ROLE_TEACHER'
        )
    )
  AND NOT EXISTS (SELECT 1 FROM teachers t WHERE t.user_id = u.id);

-- 3) Drop old FK (if it exists) before rewriting ids
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_type = 'FOREIGN KEY'
          AND table_name = 'courses'
          AND constraint_name = 'fk_courses_teacher'
    ) THEN
        ALTER TABLE courses DROP CONSTRAINT fk_courses_teacher;
    END IF;
END $$;

-- 4) Rewrite courses.teacher_id from users.id -> teachers.id
UPDATE courses c
SET teacher_id = t.id
FROM teachers t
WHERE c.teacher_user_id_tmp IS NOT NULL
  AND t.user_id = c.teacher_user_id_tmp;

-- 5) Add the enforced FK to teachers(id)
ALTER TABLE courses
    ADD CONSTRAINT fk_courses_teacher
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE SET NULL;

-- 6) Recreate view using teacher profile id
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
    t.id AS teacher_id,
    t.user_id AS teacher_user_id,
    u.username AS teacher_username,
    u.email AS teacher_email,
    t.employee_code,
    d.code AS department_code,
    d.name AS department_name
FROM courses c
LEFT JOIN subjects s ON c.subject_id = s.id
LEFT JOIN semesters sem ON c.semester_id = sem.id
LEFT JOIN teachers t ON c.teacher_id = t.id
LEFT JOIN users u ON t.user_id = u.id
LEFT JOIN departments d ON t.department_id = d.id;

-- 7) Drop temporary column
ALTER TABLE courses DROP COLUMN IF EXISTS teacher_user_id_tmp;

