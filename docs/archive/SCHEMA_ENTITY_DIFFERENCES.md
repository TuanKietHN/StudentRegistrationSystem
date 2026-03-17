# So Sánh Chi Tiết Database Schema: Entity vs Flyway SQL

Tài liệu này tổng hợp chi tiết sự khác biệt (schema drift) giữa các class Entity (JPA/Hibernate) và các file migration SQL của Flyway (`V1__init_schema.sql`) trong dự án. 

Sự sai lệch này chính là nguyên nhân gây ra các lỗi trong quá trình khởi chạy ứng dụng (DataSeeder) hoặc khi chạy migrate Flyway thủ công.

---

## 1. Lỗi Hiện Tại & Nguyên Nhân Gốc Rễ

### Lỗi 1: `ERROR: column ue1_0.avatar does not exist`
- **Nguyên nhân**: Bảng `users` trong cơ sở dữ liệu hiện tại đang thiếu các cột bổ sung như `avatar`, `failed_login_attempts`, `last_login_at`, v.v.
- **Phân tích**: Lệnh `ALTER TABLE users ADD COLUMN...` (trước đó) do quá trình migrate chưa chạy thành công (có thể do lỗi version, Flyway history bị lệch, hoặc Spring Boot chạy `DataSeeder` gọi hàm `findByEmail` trước khi Flyway migrate xong), dẫn đến việc Hibernate gọi câu lệnh `SELECT` chứa cột `avatar` nhưng DB chưa có cột này.

### Lỗi 2: `ERROR: column "assigned_at" of relation "user_roles" does not exist`
- **Nguyên nhân**: Thực thể `UserRoleEntity` có khai báo trường `assignedAt`. Tuy nhiên bản Flyway `V1__init_schema.sql` không hề tạo cột này!
- **Phân tích**: Hibernate cố gắng thực thi lệnh `INSERT INTO user_roles (assigned_at, role_id, user_id) VALUES (?, ?, ?)` khi seed data, nhưng PostgreSQL báo lỗi vì bảng không có cột `assigned_at`. 

---

## 2. Các Khác Biệt Bị Bỏ Sót (Schema Drift Chi Tiết)

Kiểm tra toàn bộ dự án, dưới đây là danh sách chi tiết những điểm lệch nhau giữa Entity và Flyway SQL:

### 2.1. Cột `assigned_at` trong bảng `user_roles`
- **Entity (`UserRoleEntity`)**: Định nghĩa cột `@Column(name = "assigned_at", nullable = false)`.
- **Flyway (`V1__init_schema.sql`)**: Chỉ định nghĩa `user_id` và `role_id`. Hoàn toàn vắng bóng cột `assigned_at`.
- **Mức độ**: 🔴 **Nghiêm trọng** (Gây lỗi INSERT khi phân quyền người dùng).

### 2.2. Cột `granted_at` trong bảng `role_permissions`
- **Entity (`RolePermissionEntity`)**: Định nghĩa cột `@Column(name = "granted_at", nullable = false)`.
- **Flyway (`V1__init_schema.sql`)**: Chỉ định nghĩa `role_id` và `permission_id`. Không tạo cột `granted_at`.
- **Mức độ**: 🔴 **Nghiêm trọng** (Sẽ gây lỗi tương tự như `user_roles` nếu ứng dụng thêm permission cho role).

### 2.3. Cột `grade` trong bảng `enrollments`
- **Entity (`EnrollmentEntity`)**: Định nghĩa trường `private Double grade;` (map trực tiếp xuống DB thành cột `grade`).
- **Flyway (`V1__init_schema.sql`)**: Bảng `enrollments` có `process_score`, `exam_score`, `final_score`, nhưng **KHÔNG CÓ** cột `grade`.
- **Mức độ**: 🔴 **Nghiêm trọng** (Lỗi khi thao tác với Enrollment).

### 2.4. Bảng `auth_audit_events` bị thiếu hoàn toàn
- **Entity (`AuthAuditEventEntity`)**: Định nghĩa `@Table(name = "auth_audit_events")` với các trường `username`, `event_type`, `success`, `ip`, `user_agent`, v.v.
- **Flyway**: Khảo sát toàn bộ script `V1` không hề có câu lệnh `CREATE TABLE auth_audit_events`.
- **Mức độ**: 🔴 **Nghiêm trọng** (Ứng dụng ghi log audit sẽ văng Exception "relation does not exist").

### 2.5. Thừa cột `created_by`, `updated_by` trong `academic_programs` & `program_subjects`
- **Entity**: `AcademicProgramEntity` và `ProgramSubjectEntity` chỉ extends `AuditEntity` (`created_at`, `updated_at`, `is_deleted`). KHÔNG định nghĩa trường `createdBy`, `updatedBy`.
- **Flyway (`V1__init_schema.sql`)**: Các bảng này lại được định nghĩa dư cột `created_by VARCHAR(255), updated_by VARCHAR(255)`.
- **Mức độ**: 🟡 **Cảnh báo** (Không gây lỗi ứng dụng trực tiếp do các cột này cho phép Null, tuy nhiên gây rác data và dư thừa schema).

### 2.6. Đồng bộ cột `users` (Nguyên nhân Flyway thất bại)
- Khi migrate thủ công gặp lỗi, lý do thường là database metadata của Flyway bị conflict (VD: Đã lỡ thay đổi thủ công schema) hoặc V1 có checksum/nội dung bị sửa so với lần chạy đầu tiên.

---

## 3. Đề Xuất Khắc Phục

Để sửa triệt để các lỗi và đồng bộ schema hai bên, bạn cần thực hiện 1 trong 2 cách sau:

**Cách 1: Tạo Script Migration Mới (Nên dùng `V3__sync_missing_columns_and_tables.sql`)**
1. Thêm `assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP` vào `user_roles`.
2. Thêm `granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP` vào `role_permissions`.
3. Thêm `grade DOUBLE PRECISION` vào `enrollments`.
4. Tạo bảng `auth_audit_events` (có các cột của `AuditEntity` và cấu trúc mapping tương ứng).

**Cách 2: Gộp chung vào V1 (Chỉ dùng nếu DB chưa có production data và chấp nhận Drop DB)**
- Hủy bỏ Database cũ (Drop schema).
- Xóa bảng `flyway_schema_history`.
- Sửa trực tiếp file `V1__init_schema.sql`: 
  - Bổ sung `assigned_at` cho `user_roles`.
  - Bổ sung `granted_at` cho `role_permissions`.
  - Bổ sung lệnh `CREATE TABLE auth_audit_events(...)`.
  - Bổ sung `grade` cho `enrollments`.
- Đảm bảo gộp đầy đủ cấu trúc vào `V1`.
- Xóa bản sửa lỗi hay file cũ và chạy lại từ DB trống.

Nếu sử dụng **Cách 2**, khởi động lại Spring Boot sẽ báo Flyway migrate thành công và DataSeeder sẽ chạy qua nhẹ nhàng không còn lỗi SQLGrammarException.
