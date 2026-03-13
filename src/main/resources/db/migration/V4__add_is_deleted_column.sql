-- Thêm cột is_deleted cho tất cả các bảng kế thừa AuditEntity
ALTER TABLE users ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE roles ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE departments ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE subjects ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE academic_programs ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE cohorts ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE semesters ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE student_classes ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE teachers ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE students ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE sections ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE enrollments ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE program_subjects ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE section_time_slots ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE permissions ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;

-- Cập nhật giá trị mặc định cho dữ liệu hiện có (nếu cần, mặc dù DEFAULT FALSE đã xử lý)
UPDATE users SET is_deleted = FALSE;
UPDATE roles SET is_deleted = FALSE;
UPDATE departments SET is_deleted = FALSE;
UPDATE subjects SET is_deleted = FALSE;
UPDATE academic_programs SET is_deleted = FALSE;
UPDATE cohorts SET is_deleted = FALSE;
UPDATE semesters SET is_deleted = FALSE;
UPDATE student_classes SET is_deleted = FALSE;
UPDATE teachers SET is_deleted = FALSE;
UPDATE students SET is_deleted = FALSE;
UPDATE sections SET is_deleted = FALSE;
UPDATE enrollments SET is_deleted = FALSE;
UPDATE program_subjects SET is_deleted = FALSE;
UPDATE section_time_slots SET is_deleted = FALSE;
UPDATE permissions SET is_deleted = FALSE;
