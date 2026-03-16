# SRD — Course Management System (CMS)

## 1) Giới thiệu hệ thống

CMS là hệ thống quản lý học phần và đăng ký học, với mục tiêu chính:
- Quản trị học kỳ, môn học, lớp học phần (course section), giảng viên/sinh viên (dưới dạng profile của user).
- Sinh viên tự đăng ký/hủy đăng ký lớp trong “cửa sổ đăng ký”, có giới hạn sĩ số và chặn trùng lịch.
- Hỗ trợ đa vai trò trên cùng một tài khoản (admin/teacher/student) ở phía UI, nhưng backend chỉ tin role trong JWT.

### Định nghĩa thuật ngữ

- **User**: tài khoản đăng nhập (auth/IAM).
- **Teacher profile**: hồ sơ giảng viên gắn với user (1-1).
- **Student profile**: hồ sơ sinh viên gắn với user (1-1).
- **Subject (Môn học)**: đơn vị kiến thức (có tín chỉ).
- **Semester (Học kỳ)**: kỳ học.
- **Course (Lớp học phần)**: một lớp của một môn trong một học kỳ, có lịch học, sĩ số, cửa sổ đăng ký.
- **Enrollment (Đăng ký)**: quan hệ student profile ↔ course.

## 2) Phạm vi chức năng hiện tại

### 2.1 Xác thực & phân quyền

- Đăng nhập (JWT access token).
- RBAC theo role trong token: ADMIN / TEACHER / STUDENT.
- Session/refresh logic theo hướng “server-side session” (Redis), không lưu refresh token trong DB.

### 2.2 Quản trị học vụ (Admin)

- CRUD Users (tạo user và gán roles).
- CRUD Semesters.
- CRUD Subjects.
- CRUD Departments.
- CRUD Teacher profiles.
- CRUD Courses (lớp học phần) với:
  - Giới hạn sĩ số (`maxStudents`, `currentStudents`)
  - `active`
  - Cửa sổ đăng ký (`enrollmentStartDate`, `enrollmentEndDate`)
  - Lịch học (time slots)

### 2.3 Sinh viên

- Xem danh sách lớp có thể đăng ký (lọc theo học kỳ/môn).
- Đăng ký lớp trong thời gian mở đăng ký.
- Hủy đăng ký trong thời gian mở đăng ký.
- Chặn trùng lịch:
  - UI hiển thị trạng thái trùng lịch dựa trên enrollments/time slots.
  - API chặn trùng lịch khi thực hiện enroll.

### 2.4 Giảng viên (hiện trạng)

- Có role TEACHER và profile teacher, nhưng UI/feature giảng dạy đang để placeholder.

## 3) Thiết kế dữ liệu (Flyway schema)

Nguồn schema: `src/main/resources/db/migration/V1__init_schema.sql`.

### 3.1 Quan hệ chính

- `users` (1) — (0..1) `teachers`
- `users` (1) — (0..1) `students`
- `subjects` (1) — (N) `courses`
- `semesters` (1) — (N) `courses`
- `teachers` (1) — (N) `courses` (teacher_id là teacher profile)
- `courses` (1) — (N) `course_time_slots`
- `students` (1) — (N) `enrollments`
- `courses` (1) — (N) `enrollments`

### 3.2 Entity theo code

Các JPA entities tương ứng (dùng để thao tác dữ liệu):
- Auth/IAM: `UserEntity`, `RoleEntity`, `UserRoleEntity`, `AuthAuditEventEntity`
- Academic: `SemesterEntity`, `SubjectEntity`, `DepartmentEntity`, `TeacherEntity`, `StudentEntity`, `CourseEntity`, `CourseTimeSlotEntity`, `EnrollmentEntity`

Ghi chú:
- `subjects.prerequisite_subject_ids` tồn tại trong schema nhưng hiện chưa mapping trong `SubjectEntity`.
- Có một số bảng “RBAC/Token” trong schema nhưng code hiện không dùng (nếu giữ schema như hiện tại thì không ảnh hưởng runtime).

## 4) Use cases (luồng chính)

### 4.1 Login

1) User nhập username/email + password.
2) Backend xác thực và trả JWT access token.
3) Frontend đọc roles trong token và điều hướng theo `activeRole`.

### 4.2 Admin tạo lớp học phần

1) Chọn Semester + Subject + Teacher (user).
2) Nhập mã lớp, tên lớp, `maxStudents`, bật/tắt `active`.
3) Thiết lập `enrollmentStartDate/enrollmentEndDate`.
4) Thiết lập time slots.

### 4.3 Student tự đăng ký

1) Student xem danh sách course đang active.
2) Bấm đăng ký:
   - API kiểm tra active, cửa sổ đăng ký, còn chỗ.
   - Kiểm tra trùng lịch theo semester + time slots.
3) Tạo enrollment và tăng `currentStudents`.

### 4.4 Student hủy đăng ký

1) Student xem danh sách enrollments của mình.
2) Hủy:
   - API kiểm tra cửa sổ đăng ký.
   - Giảm `currentStudents` và xóa enrollment.

## 5) Hệ thống đang giống “đăng ký học đại học” hay “LMS nội bộ công ty”?

Theo mô hình dữ liệu và nghiệp vụ hiện tại, hệ thống gần với **đăng ký học đại học** hơn:
- Course = lớp học phần (1 subject + 1 semester), có lịch học và trùng lịch.
- Enrollment window giống “đợt đăng ký học phần”.

Tuy nhiên, dùng cho nội bộ công ty vẫn hợp nếu coi course là “lớp đào tạo theo đợt”.

### Có cần giới hạn số lượng người đăng ký nếu là công ty?

Vẫn nên có giới hạn, vì:
- Giới hạn phòng học/số thiết bị.
- Giới hạn giảng viên/trainer.
- Giới hạn chất lượng (tỉ lệ trainer:trainee).
Nếu là e-learning thuần online và không cần lịch học, có thể bỏ trùng lịch và nới maxStudents.

## 6) Quyết định mô hình: credits, status, course–subject

### 6.1 Credits (tín chỉ) nên thuộc Course hay Subject?

Khuyến nghị thực tế:
- **Credits thuộc Subject** (chuẩn học vụ).
- Course (lớp học phần) kế thừa/hiển thị credits của subject khi cần.

Hiện code đang theo hướng này: `Subject.credits` có, `Course` không có `credits`.

### 6.2 Status nên thuộc Course hay Subject?

Khuyến nghị:
- `Subject.active`: môn có còn dạy trong chương trình hay không.
- `Course.active`: lớp học phần kỳ này có mở hay không.
- Nếu cần trạng thái phong phú hơn: thêm enum cho Course (DRAFT/OPEN/CLOSED/CANCELED) thay vì nhét vào Subject.

Hiện code dùng `active` cho cả subject và course, và có enrollment window để quyết định “mở/đóng”.

### 6.3 Một Course gồm nhiều môn học hay một môn học gồm nhiều chương?

Hiện mô hình là:
- **1 Course ↔ 1 Subject** (một lớp học phần của một môn).
- “Nhiều chương”/“mục lục” là cấu trúc nội dung của Subject/Course kiểu LMS, hiện chưa có trong schema/code.

Nếu muốn LMS nội dung:
- Thêm bảng `modules/lessons/assignments` gắn với Subject hoặc Course (tuỳ mục tiêu).

## 7) Flyway làm trung tâm schema có bị ảnh hưởng bởi các vấn đề trên không?

Không bị “kẹt”, nhưng cần quy ước thay đổi:
- Nếu đổi mô hình (thêm status enum, thêm modules/lessons, đổi quan hệ), chỉ cần viết migration mới và cập nhật code tương ứng.
- Tránh sửa lại lịch sử schema nếu đã có môi trường chạy thật; dùng versioned migrations để tiến hoá.

## 8) Các bài toán LMS nâng cao: dồn lớp khi ít người học

Hiện code **chưa** xử lý “dồn lớp/ghép lớp” tự động.

Để làm được cần thêm nghiệp vụ & dữ liệu:
- Có `minStudents` cho course.
- Cơ chế “merge sections”:
  - Chọn lớp đích trong cùng subject+semester.
  - Chuyển enrollments và cập nhật sĩ số.
  - Kiểm tra trùng lịch (vì các lớp khác nhau có time slots khác nhau).
  - Thông báo cho sinh viên/giảng viên.

### Dồn lớp khi nào?

Khuyến nghị thực tế:
- **Sau khi đóng đăng ký** (freeze danh sách) và **trước khi bắt đầu học**.
- Nếu dồn trước khi mở đăng ký: không cần “move enrollments”, chỉ cần điều chỉnh kế hoạch mở lớp.

### Dồn không hết thì xử lý thế nào?

Các lựa chọn thường dùng:
- Hủy một số lớp (CANCELED) và hoàn/giữ chỗ cho sinh viên.
- Mở thêm một “đợt đăng ký bổ sung” để sinh viên chọn lại lớp phù hợp.
- Cho phép override trùng lịch theo phê duyệt (thường không khuyến nghị).

Đề xuất triển khai tương lai:
- Thêm trạng thái Course: OPEN/CLOSED/CANCELED.
- Thêm use case Admin “Merge/Cancel Sections”.

## 9) Roadmap triển khai (đề xuất)

- P1 (đã có phần lớn): UI sinh viên đăng ký/hủy + hiển thị lịch + trùng lịch.
- P2: UI giảng viên (danh sách lớp, cập nhật điểm/trạng thái enrollment).
- P3: Chuẩn hoá lifecycle lớp:
  - Course status enum
  - Job/endpoint đóng đăng ký
  - Use case dồn lớp/hủy lớp sau đăng ký
- P4: Nếu cần LMS nội dung: modules/lessons/assignments (tách riêng khỏi course section).

