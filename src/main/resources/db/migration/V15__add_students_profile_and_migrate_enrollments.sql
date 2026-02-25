-- =====================================================
-- Migration V15: Add students profile and migrate enrollments.student_id to students(id)
-- =====================================================

CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    student_code VARCHAR(50) NOT NULL UNIQUE,
    department_id BIGINT,
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

CREATE INDEX idx_students_user_id ON students(user_id);
CREATE INDEX idx_students_student_code ON students(student_code);
CREATE INDEX idx_students_department_id ON students(department_id);
CREATE INDEX idx_students_active ON students(active);

-- Backfill student profiles for users that are STUDENT (RBAC) or legacy users.role column.
INSERT INTO students (user_id, student_code, active, created_at, updated_at)
SELECT u.id,
       ('SV' || u.id::text) AS student_code,
       TRUE,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM users u
WHERE (
          EXISTS (
              SELECT 1
              FROM user_roles ur
              JOIN roles r ON r.id = ur.role_id
              WHERE ur.user_id = u.id AND r.name = 'ROLE_STUDENT'
          )
          OR u.role = 'ROLE_STUDENT'
      )
  AND NOT EXISTS (SELECT 1 FROM students s WHERE s.user_id = u.id);

-- Ensure any user referenced by existing enrollments also has a student profile.
INSERT INTO students (user_id, student_code, active, created_at, updated_at)
SELECT DISTINCT e.student_id,
       ('SV' || e.student_id::text) AS student_code,
       TRUE,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM enrollments e
LEFT JOIN students s ON s.user_id = e.student_id
WHERE s.id IS NULL;

-- Migrate enrollments.student_id from users(id) to students(id)
ALTER TABLE enrollments
    ADD COLUMN student_profile_id BIGINT;

UPDATE enrollments e
SET student_profile_id = s.id
FROM students s
WHERE s.user_id = e.student_id;

ALTER TABLE enrollments
    ALTER COLUMN student_profile_id SET NOT NULL;

ALTER TABLE enrollments
    DROP CONSTRAINT IF EXISTS uk_enrollments_course_student;

ALTER TABLE enrollments
    DROP CONSTRAINT IF EXISTS fk_enrollments_student;

ALTER TABLE enrollments
    DROP COLUMN student_id;

ALTER TABLE enrollments
    RENAME COLUMN student_profile_id TO student_id;

ALTER TABLE enrollments
    ADD CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE;

ALTER TABLE enrollments
    ADD CONSTRAINT uk_enrollments_course_student UNIQUE (course_id, student_id);

