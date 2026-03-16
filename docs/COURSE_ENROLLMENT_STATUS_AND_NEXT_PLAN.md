# Tổng hợp hiện trạng & kế hoạch triển khai: Đăng ký lớp học (Enrollments)

## 1) Backend: trạng thái hiện tại

### 1.1 Enrollment APIs (đã siết quyền và loại bỏ IDOR)

- Admin đăng ký hộ: `POST /api/v1/enrollments` (ADMIN)
  - Payload: `courseId` + `studentId` (Student Profile ID): [EnrollmentCreateRequest](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/dto/EnrollmentCreateRequest.java)
  - Controller: [EnrollmentController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/EnrollmentController.java)
- Sinh viên tự đăng ký: `POST /api/v1/enrollments/self` (STUDENT)
  - Payload: `courseId`: [EnrollmentSelfRequest](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/dto/EnrollmentSelfRequest.java)
- Hủy đăng ký: `DELETE /api/v1/enrollments/{id}` (ADMIN/STUDENT)
  - Rule: chỉ được đăng ký/hủy trong enrollment window: [EnrollmentServiceImpl](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/application/EnrollmentServiceImpl.java)
- Danh sách của tôi: `GET /api/v1/enrollments/me` (STUDENT)
- Danh sách theo student profile: `GET /api/v1/enrollments/student/{studentId}` (ADMIN)
- Danh sách theo lớp: `GET /api/v1/enrollments/course/{courseId}` (ADMIN/TEACHER)

### 1.2 Student profile (đã bổ sung, tương tự Teacher)

- `students` là profile của `users` (One-to-One qua `user_id`) và `enrollments.student_id` đã migrate sang `students(id)`:
  - Migration: [V15__add_students_profile_and_migrate_enrollments.sql](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/resources/db/migration/V15__add_students_profile_and_migrate_enrollments.sql)
  - API quản trị student profile: [StudentController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/StudentController.java)

### 1.3 Enrollment window (deadline)

- Course có `enrollmentStartDate` và `enrollmentEndDate` (DATE), được expose qua API course và được enforce khi đăng ký/hủy:
  - Backfill mặc định cho dữ liệu cũ: [V16__add_course_time_slots_and_backfill_enrollment_window.sql](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/resources/db/migration/V16__add_course_time_slots_and_backfill_enrollment_window.sql)
  - Rule enforce: [EnrollmentServiceImpl](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/application/EnrollmentServiceImpl.java)

### 1.4 Chặn trùng lịch (API) + API quản trị lịch

- Bổ sung `course_time_slots` và rule chặn trùng lịch khi đăng ký:
  - Migration: [V16__add_course_time_slots_and_backfill_enrollment_window.sql](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/resources/db/migration/V16__add_course_time_slots_and_backfill_enrollment_window.sql)
  - Chặn trùng lịch tại enroll: [EnrollmentServiceImpl](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/application/EnrollmentServiceImpl.java)
- API quản trị lịch học theo lớp:
  - `GET /api/v1/courses/{id}/time-slots`
  - `PUT /api/v1/courses/{id}/time-slots` (ADMIN)
  - Controller: [CourseController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/CourseController.java)

## 2) Frontend: trạng thái hiện tại

- Hiện chưa có UI cho sinh viên tự đăng ký lớp (chưa có route/view/service enrollments).
- Màn hình `Courses` hiện là UI quản trị (CRUD) và chưa có phần set enrollment window / time slots:
  - Route `/courses`: [router/index.ts](file:///c:/Users/Admin/Downloads/Repo/cms/frontend/src/router/index.ts#L16-L82)
  - View: [CourseList.vue](file:///c:/Users/Admin/Downloads/Repo/cms/frontend/src/views/academic/CourseList.vue#L4-L101)

## 3) Quy định nghiệp vụ (đã chốt)

- Student được đăng ký và hủy đăng ký.
- Có deadline theo khoảng thời gian mở đăng ký (admin quyết định).
- Hết thời gian mở đăng ký: không đăng ký và không hủy được.
- Đăng ký không cần admin duyệt.
- Có chặn trùng lịch:
  - UI phải hiển thị thông báo.
  - API phải chặn (đã triển khai).

## 4) Plan triển khai UI tiếp theo (theo standards multi-role)

### P1 — UI sinh viên

1) Trang “Đăng ký lớp”:
   - Danh sách course có thể đăng ký (đang mở đăng ký, còn chỗ, active).
   - Hiển thị enrollment window + time slots.
   - Nút “Đăng ký” gọi `POST /api/v1/enrollments/self`.
2) Trang “Lớp đã đăng ký”:
   - Danh sách từ `GET /api/v1/enrollments/me`.
   - Nút “Hủy” gọi `DELETE /api/v1/enrollments/{id}` (API sẽ chặn nếu hết hạn).

### P2 — UI admin/teacher

- Admin:
  - Quản trị student profiles (nếu cần).
  - Quản trị enrollment window và time slots trong form course.
  - Đăng ký hộ: `POST /api/v1/enrollments`.
- Teacher:
  - Danh sách sinh viên trong lớp + cập nhật điểm/trạng thái.

## 5) Tương thích “One Login Form + Role-based Redirect + Role Switcher”

- Backend hiện đã là 1 login form (đúng), và authz enforce ở API layer (đúng).
- Để implement Role Switcher đúng chuẩn:
  - Frontend quản lý `activeRole` trong Pinia + persist `lastActiveRole` (không đưa vào JWT).
  - Route guards enforce theo `meta.roles` và `meta.activeRoleRequired` (không kiểm tra role rải rác trong component).
  - Backend không trust `activeRole` client gửi; chỉ dựa vào JWT roles/authorities và `@PreAuthorize`.

