-- Rename core academic tables to match new terminology:
-- subjects  -> classes
-- courses   -> cohorts
-- course_time_slots -> cohort_time_slots

ALTER TABLE subjects RENAME TO classes;
ALTER SEQUENCE subjects_id_seq RENAME TO classes_id_seq;
ALTER TABLE classes ALTER COLUMN id SET DEFAULT nextval('classes_id_seq');

ALTER TABLE courses RENAME TO cohorts;
ALTER SEQUENCE courses_id_seq RENAME TO cohorts_id_seq;
ALTER TABLE cohorts ALTER COLUMN id SET DEFAULT nextval('cohorts_id_seq');

ALTER TABLE course_time_slots RENAME TO cohort_time_slots;
ALTER SEQUENCE course_time_slots_id_seq RENAME TO cohort_time_slots_id_seq;
ALTER TABLE cohort_time_slots ALTER COLUMN id SET DEFAULT nextval('cohort_time_slots_id_seq');

ALTER TABLE cohorts RENAME COLUMN subject_id TO class_id;
ALTER TABLE cohorts RENAME COLUMN merged_into_course_id TO merged_into_cohort_id;

ALTER TABLE enrollments RENAME COLUMN course_id TO cohort_id;
ALTER TABLE cohort_time_slots RENAME COLUMN course_id TO cohort_id;

ALTER TABLE attendance_sessions RENAME COLUMN course_id TO cohort_id;

DROP VIEW IF EXISTS v_courses_with_teacher;
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
LEFT JOIN departments d ON t.department_id = d.id;

ALTER TRIGGER trg_update_courses_updated_at ON cohorts RENAME TO trg_update_cohorts_updated_at;
ALTER TRIGGER trg_update_subjects_updated_at ON classes RENAME TO trg_update_classes_updated_at;
