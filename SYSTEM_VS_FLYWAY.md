# System vs Flyway Comparison

## 1. Entity vs Table Mapping

| Java Entity | Database Table | Status | Notes |
| :--- | :--- | :--- | :--- |
| `UserEntity` / `User` | `users` | ✅ Aligned | Roles được map qua `user_roles`. Cột legacy `users.role` đã drop ở V7. |
| `RoleEntity` | `roles` | ✅ Aligned | `RoleType` map theo `ROLE_` prefix trong mapper. |
| `SemesterEntity` / `Semester` | `semesters` | ✅ Aligned | `code`, `name`, `start_date`, `end_date`, `active`. |
| `SubjectEntity` / `Subject` | `subjects` | ✅ Aligned | Dùng cột `credits`; cột legacy `credit` được hợp nhất và drop ở V11. |
| `CourseEntity` / `Course` | `courses` | ✅ Aligned | Chuẩn hoá `courses` là lớp học phần (V1): gắn `semester_id`, `subject_id`, quota, `teacher_id -> teachers(id)` và teachers link sang users qua `teachers.user_id`. |
| `TeacherEntity` | `teachers` | ✅ Aligned | Teacher là profile tuỳ chọn của user: `teachers.user_id` (UNIQUE). |
| `DepartmentEntity` | `departments` | ✅ Aligned | `head_teacher_id -> teachers(id)` (profile). |

## 2. Key Discrepancies

### 2.1. Flyway vs Hibernate schema ownership
- Flyway là nguồn schema duy nhất.
- JPA/Hibernate đặt ở chế độ `validate` để phát hiện lệch schema thay vì tự thay đổi schema.

### 2.2. Data Seeding Strategy
- **Flyway:** Seed dữ liệu nền tảng/định danh (roles, permissions, user mặc định nếu cần).
- **Application Seeder:** Chỉ nên dùng cho demo/dev, có guard theo profile/property, và idempotent.

### 2.3. Course/Teacher normalization
- `courses.teacher_id` luôn là `users.id`.
- Teacher profile lấy từ `teachers` qua `teachers.user_id` (xem view `v_courses_with_teacher` được sửa ở V10).

## 3. Summary
System và Flyway đã được chuẩn hoá để nhất quán: Flyway quản trị schema; JPA validate; Course theo V1; Teacher là profile tuỳ chọn của User.
