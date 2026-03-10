INSERT INTO permissions (name, description, module) VALUES 
('academic_program:read', 'Xem chương trình đào tạo', 'ACADEMIC_PROGRAM'),
('academic_program:create', 'Tạo chương trình đào tạo', 'ACADEMIC_PROGRAM'),
('academic_program:update', 'Cập nhật chương trình đào tạo', 'ACADEMIC_PROGRAM'),
('academic_program:delete', 'Xóa chương trình đào tạo', 'ACADEMIC_PROGRAM'),
('student_progress:read_all', 'Xem tiến độ học tập (Tất cả)', 'STUDENT_PROGRESS'),
('student_progress:read_class', 'Xem tiến độ học tập (Lớp chủ nhiệm)', 'STUDENT_PROGRESS'),
('student_progress:read_self', 'Xem tiến độ học tập (Cá nhân)', 'STUDENT_PROGRESS');

-- Assign permissions to roles
-- ADMIN: Full access to academic programs and student progress
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN' 
AND p.name IN (
    'academic_program:read', 
    'academic_program:create', 
    'academic_program:update', 
    'academic_program:delete',
    'student_progress:read_all'
);

-- TEACHER: Read academic programs, Read progress of homeroom class
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_TEACHER' 
AND p.name IN (
    'academic_program:read',
    'student_progress:read_class'
);

-- STUDENT: Read academic programs, Read own progress
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_STUDENT' 
AND p.name IN (
    'academic_program:read',
    'student_progress:read_self'
);
