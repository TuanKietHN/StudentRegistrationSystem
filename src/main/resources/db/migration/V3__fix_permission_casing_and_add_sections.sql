-- 1. Fix casing for existing permissions (lowercase to UPPERCASE)
UPDATE permissions SET name = UPPER(name) WHERE name LIKE 'academic_program:%';
UPDATE permissions SET name = UPPER(name) WHERE name LIKE 'student_progress:%';

-- 2. Add SECTION permissions if not exist
INSERT INTO permissions (name, description, resource, action)
SELECT 'SECTION:READ', 'Xem lớp học phần', 'SECTION', 'READ'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SECTION:READ');

INSERT INTO permissions (name, description, resource, action)
SELECT 'SECTION:CREATE', 'Tạo lớp học phần', 'SECTION', 'CREATE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SECTION:CREATE');

INSERT INTO permissions (name, description, resource, action)
SELECT 'SECTION:UPDATE', 'Cập nhật lớp học phần', 'SECTION', 'UPDATE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SECTION:UPDATE');

INSERT INTO permissions (name, description, resource, action)
SELECT 'SECTION:DELETE', 'Xóa lớp học phần', 'SECTION', 'DELETE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SECTION:DELETE');

-- 3. Add ENROLLMENT permissions if not exist
INSERT INTO permissions (name, description, resource, action)
SELECT 'ENROLLMENT:READ', 'Xem đăng ký học phần', 'ENROLLMENT', 'READ'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ENROLLMENT:READ');

INSERT INTO permissions (name, description, resource, action)
SELECT 'ENROLLMENT:CREATE', 'Tạo đăng ký học phần', 'ENROLLMENT', 'CREATE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ENROLLMENT:CREATE');

INSERT INTO permissions (name, description, resource, action)
SELECT 'ENROLLMENT:UPDATE', 'Cập nhật đăng ký học phần', 'ENROLLMENT', 'UPDATE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ENROLLMENT:UPDATE');

INSERT INTO permissions (name, description, resource, action)
SELECT 'ENROLLMENT:DELETE', 'Xóa đăng ký học phần', 'ENROLLMENT', 'DELETE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ENROLLMENT:DELETE');

-- 4. Add STUDENT_CLASS permissions if not exist
INSERT INTO permissions (name, description, resource, action)
SELECT 'STUDENT_CLASS:READ', 'Xem lớp hành chính', 'STUDENT_CLASS', 'READ'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'STUDENT_CLASS:READ');

INSERT INTO permissions (name, description, resource, action)
SELECT 'STUDENT_CLASS:CREATE', 'Tạo lớp hành chính', 'STUDENT_CLASS', 'CREATE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'STUDENT_CLASS:CREATE');

INSERT INTO permissions (name, description, resource, action)
SELECT 'STUDENT_CLASS:UPDATE', 'Cập nhật lớp hành chính', 'STUDENT_CLASS', 'UPDATE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'STUDENT_CLASS:UPDATE');

INSERT INTO permissions (name, description, resource, action)
SELECT 'STUDENT_CLASS:DELETE', 'Xóa lớp hành chính', 'STUDENT_CLASS', 'DELETE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'STUDENT_CLASS:DELETE');


-- 5. Assign SECTION permissions to ADMIN (Full)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN' 
AND p.name LIKE 'SECTION:%'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 6. Assign SECTION:READ to TEACHER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_TEACHER' 
AND p.name = 'SECTION:READ'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 7. Assign ENROLLMENT permissions to ADMIN (Full)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN' 
AND p.name LIKE 'ENROLLMENT:%'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 8. Assign ENROLLMENT:READ, ENROLLMENT:UPDATE to TEACHER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_TEACHER' 
AND p.name IN ('ENROLLMENT:READ', 'ENROLLMENT:UPDATE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 9. Assign ENROLLMENT:READ, ENROLLMENT:CREATE, ENROLLMENT:DELETE to STUDENT
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_STUDENT' 
AND p.name IN ('ENROLLMENT:READ', 'ENROLLMENT:CREATE', 'ENROLLMENT:DELETE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 10. Assign STUDENT_CLASS permissions to ADMIN (Full)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN' 
AND p.name LIKE 'STUDENT_CLASS:%'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 11. Assign STUDENT_CLASS:READ to TEACHER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_TEACHER' 
AND p.name = 'STUDENT_CLASS:READ'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
