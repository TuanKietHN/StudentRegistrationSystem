INSERT INTO permissions (name, description, resource, action) VALUES 
('ACADEMIC_PROGRAM:READ', 'Xem chương trình đào tạo', 'ACADEMIC_PROGRAM', 'READ'),
('ACADEMIC_PROGRAM:CREATE', 'Tạo chương trình đào tạo', 'ACADEMIC_PROGRAM', 'CREATE'),
('ACADEMIC_PROGRAM:UPDATE', 'Cập nhật chương trình đào tạo', 'ACADEMIC_PROGRAM', 'UPDATE'),
('ACADEMIC_PROGRAM:DELETE', 'Xóa chương trình đào tạo', 'ACADEMIC_PROGRAM', 'DELETE'),
('STUDENT_PROGRESS:READ_ALL', 'Xem tiến độ học tập (Tất cả)', 'STUDENT_PROGRESS', 'READ_ALL'),
('STUDENT_PROGRESS:READ_CLASS', 'Xem tiến độ học tập (Lớp chủ nhiệm)', 'STUDENT_PROGRESS', 'READ_CLASS'),
('STUDENT_PROGRESS:READ_SELF', 'Xem tiến độ học tập (Cá nhân)', 'STUDENT_PROGRESS', 'READ_SELF');

-- Assign permissions to roles
-- ADMIN: Full access to academic programs and student progress
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN' 
AND p.name IN (
    'ACADEMIC_PROGRAM:READ', 
    'ACADEMIC_PROGRAM:CREATE', 
    'ACADEMIC_PROGRAM:UPDATE', 
    'ACADEMIC_PROGRAM:DELETE',
    'STUDENT_PROGRESS:READ_ALL'
);

-- TEACHER: Read academic programs, Read progress of homeroom class
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_TEACHER' 
AND p.name IN (
    'ACADEMIC_PROGRAM:READ',
    'STUDENT_PROGRESS:READ_CLASS'
);

-- STUDENT: Read academic programs, Read own progress
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_STUDENT' 
AND p.name IN (
    'ACADEMIC_PROGRAM:READ',
    'STUDENT_PROGRESS:READ_SELF'
);
