-- =====================================================
-- V9__remove_online_learning_schema.sql
-- Scope decision: Remove LMS/e-learning schema (V4-V6)
-- Reason: Project standardizes Course as V1 "lớp học phần" only.
-- =====================================================

-- Drop LMS/assessment/attendance views (safe, idempotent)
DROP VIEW IF EXISTS v_teacher_session_schedule CASCADE;
DROP VIEW IF EXISTS v_student_attendance_summary CASCADE;
DROP VIEW IF EXISTS v_student_course_progress CASCADE;

-- Drop attendance tables
DROP TABLE IF EXISTS attendance_qr_codes CASCADE;
DROP TABLE IF EXISTS attendance CASCADE;
DROP TABLE IF EXISTS class_sessions CASCADE;

-- Drop assessment tables
DROP TABLE IF EXISTS quiz_attempt_answers CASCADE;
DROP TABLE IF EXISTS quiz_attempts CASCADE;
DROP TABLE IF EXISTS quiz_answers CASCADE;
DROP TABLE IF EXISTS quiz_questions CASCADE;
DROP TABLE IF EXISTS quizzes CASCADE;
DROP TABLE IF EXISTS assignment_submission_files CASCADE;
DROP TABLE IF EXISTS assignment_submissions CASCADE;
DROP TABLE IF EXISTS assignments CASCADE;

-- Drop lesson/content/progress tables
DROP TABLE IF EXISTS student_lesson_progress CASCADE;
DROP TABLE IF EXISTS lesson_attachments CASCADE;
DROP TABLE IF EXISTS lesson_contents CASCADE;
DROP TABLE IF EXISTS lessons CASCADE;
DROP TABLE IF EXISTS lesson_sections CASCADE;

-- Drop helper functions from V4-V6 (safe, idempotent)
DROP FUNCTION IF EXISTS update_enrollment_progress() CASCADE;
DROP FUNCTION IF EXISTS auto_create_attendance_records() CASCADE;
DROP FUNCTION IF EXISTS calculate_session_duration() CASCADE;
DROP FUNCTION IF EXISTS calculate_late_minutes() CASCADE;
DROP FUNCTION IF EXISTS refresh_course_statistics() CASCADE;

