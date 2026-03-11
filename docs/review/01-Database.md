# 01. Database Review — Schema, Migrations & Entity/Domain Mapping

> Cập nhật: 2026-03-11 — phản ánh trạng thái hiện tại sau khi fix migration.

## 1) Chiến lược quản lý schema

| Tiêu chí              | Trạng thái          |
|------------------------|---------------------|
| Flyway                 | ✅ Enabled (auto-config) |
| `ddl-auto`             | ✅ `validate`       |
| Migration files        | 3 files (V1 consolidated, V2, V3) |
| Nguồn schema duy nhất  | ✅ Flyway           |

- **V1__init_schema.sql** (consolidated V1–V25): Schema toàn bộ hệ thống, 495 dòng, tạo mới tất cả bảng + seed RBAC + view `v_sections_with_teacher`.
- **V2__add_academic_permissions.sql**: Bổ sung permissions cho Academic Program và Student Progress.
- **V3__fix_permission_casing_and_add_sections.sql**: Sửa lỗi case hoa thường permissions + bổ sung section permissions.

Kết luận: Schema đã được hợp nhất (consolidated) rất clean, tránh được vấn đề drift từ nhiều migration nhỏ.

## 2) Nhóm bảng theo bounded context

### 2.1. Auth / IAM (9 bảng)

| Bảng                 | Vai trò |
|----------------------|---------|
| `users`              | Tài khoản đăng nhập (username, email, password) |
| `roles`              | ROLE_ADMIN, ROLE_TEACHER, ROLE_STUDENT |
| `user_roles`         | M-N: user ↔ role |
| `permissions`        | RESOURCE:ACTION (48+ permissions) |
| `role_permissions`   | M-N: role ↔ permission |
| `auth_audit_events`  | Ghi lại login/logout/failed attempts (từ code, chưa thấy trong V1 migration — **cần kiểm tra**) |

Entity hiện có:
- `UserEntity`, `RoleEntity`, `UserRoleEntity`, `UserRoleId` — map đầy đủ
- `PermissionEntity`, `RolePermissionEntity`, `RolePermissionId` — map đầy đủ
- `AuthAuditEventEntity` — ✅ entity có nhưng **migration chưa rõ** (có thể tạo qua `ddl-auto` hoặc migration riêng)

> [!WARNING]
> `AuthAuditEventEntity` không thấy trong V1__init_schema.sql. Cần xác minh bảng `auth_audit_events` được tạo từ đâu (migration missing? hay JPA auto?). Đây là rủi ro nếu `ddl-auto=validate` thì sẽ fail khi bảng chưa tồn tại.

### 2.2. Academic (12 bảng)

| Bảng                 | Vai trò |
|----------------------|---------|
| `departments`        | Khoa/bộ môn (hỗ trợ parent-child) |
| `teachers`           | Hồ sơ giảng viên, FK → users (1:1) |
| `subjects`           | Môn học (code, credits, process_weight, exam_weight) |
| `semesters`          | Học kỳ (active/secondary_active) |
| `cohorts`            | Niên khóa (start_year – end_year) |
| `academic_programs`  | Chương trình đào tạo (FK → departments) |
| `program_subjects`   | M-N: chương trình ↔ môn học (semester, type, pass_score) |
| `student_classes`    | Lớp hành chính (FK → department, cohort, teacher, program) |
| `students`           | Hồ sơ sinh viên, FK → users (1:1) + student_class |
| `sections`           | Lớp học phần (FK → semester, subject, user/teacher) |
| `section_time_slots` | Lịch học (day_of_week, start/end time, room) |
| `enrollments`        | Đăng ký học phần + điểm (process/exam/final score, score locking, override) |

Entity → bảng: tất cả 12 entity đã **map đầy đủ** với bảng tương ứng.

## 3) Các điểm cần chú ý

### 3.1. `sections.teacher_id` → `users(id)` (KHÔNG phải `teachers(id)`)

Schema hiện tại: `sections.teacher_id` FK → `users(id)`.

Trong code:
- `Section.teacher` (domain model) có kiểu `User`, không phải `Teacher`
- `SectionEntity.teacher` map tới `UserEntity`
- View `v_sections_with_teacher` JOIN đúng: `sections.teacher_id = users.id`, rồi JOIN `teachers.user_id = users.id`

**Thiết kế này hợp lệ** nhưng có nhược điểm:
- Không enforce ràng buộc "chỉ user có teacher profile mới được gán dạy" ở tầng DB
- Muốn enforce cần ALTER FK thành `sections.teacher_id → teachers(id)` (hoặc validation ở tầng service)

→ **Service tầng hiện tại đã validate**: DataSeeder chỉ gán user có ROLE_TEACHER.

### 3.2. `auth_audit_events` — thiếu migration

`AuthAuditEventEntity` có trong code nhưng bảng không nằm trong V1. Cần tạo migration mới hoặc xác nhận bảng đã tạo bằng cách khác (ví dụ: `ddl-auto=update` trước đó rồi giữ lại).

### 3.3. V2/V3 Migrations (Đã sửa)

Trước đó V2/V3 dùng cột `module` không tồn tại trong V1. Hiện tại đã được sửa thành `resource` và `action` để đồng bộ hoàn toàn với schema của V1.

### 3.4. RefreshToken / PasswordResetToken — quản lý bằng Redis

Không thấy bảng `refresh_tokens` hay `password_reset_tokens` trong V1 consolidated. Từ code:
- Refresh token được quản lý qua `RedisSessionRepository` (Redis-backed sessions)
- Password reset token qua `PasswordService` (cần xác nhận xem lưu ở Redis hay DB)

→ Hướng tiếp cận Redis-backed session là đúng cho production, nhưng cần document rõ dependency vào Redis.

## 4) Kết luận

### ✅ Đã tốt

- Schema consolidated V1 rất sạch, đầy đủ indexes, triggers, constraints
- Grade weights có CHECK constraint (`process_weight + exam_weight = 100`)
- Enrollment có UNIQUE constraint (`section_id, student_id`), tránh đăng ký trùng
- Section lifecycle (OPEN/CLOSED/CANCELED/MERGED) có CHECK constraint
- View `v_sections_with_teacher` phục vụ query join phức tạp
- Schema hỗ trợ score locking + score override (audit trail cho điểm)

### ⚠️ Cần xử lý

| # | Vấn đề | Mức độ |
|---|--------|--------|
| 1 | `auth_audit_events` thiếu migration | 🔴 Cao |
| 2 | Xác nhận Redis dependency cho session/token | 🟡 Trung bình |
| 3 | `sections.teacher_id` FK tới `users` thay vì `teachers` | 🟢 Chấp nhận được |
| 4 | `subjects` không có bảng prerequisite riêng (nếu cần prerequisites) | 🟡 Trung bình |
