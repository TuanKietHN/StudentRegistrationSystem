# 01. Database review (migrations ↔ entity/domain)

## 1) Thiết lập hiện tại

### Backend đang chạy theo hướng nào?
- Flyway là nguồn schema duy nhất (enabled + baseline-on-migrate)
- `spring.jpa.hibernate.ddl-auto=validate` để JPA chỉ validate schema

Mục tiêu là tránh drift schema: schema chỉ được thay đổi qua migrations, ứng dụng chỉ validate.

Nguồn:
- `application.properties`
- Migrations: `src/main/resources/db/migration/V1..V11`

## 2) Nhóm bảng theo bounded context

### 2.1. Auth / IAM

Schema theo migrations:
- `users` (V1, V7 drop `role`, V8 add `avatar`)
- `roles`, `permissions`, `role_permissions`, `user_roles` (V2)
- `refresh_tokens`, `password_reset_tokens` (V2)

Entity hiện có:
- `UserEntity` → `users`
- `RoleEntity`, `UserRoleEntity` → `roles`, `user_roles`

Nhận xét:
- Dữ liệu “permission” đã có trong DB (migrations) nhưng hiện chưa thấy entity/service/controller nào dùng `permissions`, `role_permissions` để authorize theo permission. Code hiện thiên về `@PreAuthorize(hasRole(...))`.

### 2.2. Academic (quản lý học vụ cơ bản)

Schema theo migrations (đang được API sử dụng):
- `semesters` (V1)
- `subjects` (V1, V3 mở rộng)
- `courses` (V1, V3 mở rộng)
- `enrollments` (V1)

Entity hiện có:
- `SemesterEntity` → `semesters`
- `SubjectEntity` → `subjects`
- `CourseEntity` → `courses`
- `EnrollmentEntity` → `enrollments`
- `DepartmentEntity` → `departments` (V3)
- `TeacherEntity` → `teachers` (V3)

Nhận xét nổi bật:
- API/Service hiện gọi `Course` là “lớp học phần”, tức một course gắn với `semester_id` và `subject_id` (mô hình V1).
- Phần schema LMS (V4–V6) đã bị loại khỏi scope và bị drop bằng V9.

## 3) Các điểm lệch schema ↔ entity/domain cần chú ý

### 3.1. `courses.teacher_id`: Users hay Teachers?

V1:
- `courses.teacher_id` FK → `users(id)`

V3:
- Tạo bảng `teachers(user_id, ...)`
- View `v_courses_with_teacher` JOIN `courses.teacher_id = teachers.id` (ngụ ý `teacher_id` là teachers.id)
- Nhưng migration không ALTER FK của `courses.teacher_id`, nên về mặt schema gốc vẫn theo V1

Trong code:
- `CourseEntity.teacher` đang map sang `UserEntity` (users)

Hệ quả:
- View `v_courses_with_teacher` có nguy cơ sai (JOIN nhầm cột) nếu `teacher_id` thực tế là user_id.

Khuyến nghị chuẩn hoá (chọn 1 trong 2 hướng):
1. Chuẩn hoá `courses.teacher_id` → `teachers(id)` để enforce ở DB (V12), và join sang users qua `teachers.user_id`.

### 3.2. `subjects.credit` vs `subjects.credits`

V1 tạo cột `credit`.
V3 thêm cột `credits` (mặc định 3).

Trong code:
- `SubjectEntity`/domain sử dụng `credits`.

Hệ quả:
- Schema có khả năng tồn tại đồng thời 2 cột cùng ý nghĩa; dữ liệu seed ở V1 đi vào `credit` sẽ không tự “điền” sang `credits` nếu không có migration chuyển đổi.

Khuyến nghị:
- Đã hợp nhất bằng V11 (copy dữ liệu và drop `credit`).

## 4) Kết luận nhanh về DB readiness cho “quản lý khóa học nội bộ”

Phần đã “đủ dùng” (có API + entity + domain):
- Auth cơ bản + JWT
- Quản lý người dùng (admin) + avatar (MinIO)
- Môn học (Subject), Học kỳ (Semester), Lớp học phần (Course), Đăng ký (Enrollment)

Điểm cần xử lý trước khi mở rộng (theo hướng nội bộ):
- Bổ sung module teachers/departments ở layer API/service nếu cần quản trị hồ sơ giảng viên/khoa
- Mở rộng lớp học phần với lịch học/phòng học/điểm danh theo buổi (nếu nghiệp vụ yêu cầu)
