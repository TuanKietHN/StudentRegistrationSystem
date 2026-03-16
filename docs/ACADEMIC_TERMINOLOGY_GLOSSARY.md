# Thuật ngữ Module Academic (VN/EN) + Mapping Entity/DB + Quy ước đặt tên

Tài liệu này tổng hợp:
- Khái niệm đang dùng trong dự án (nghĩa thực tế hiện tại).
- Từ khóa (keywords) hay xuất hiện trong code/UI.
- Entity (Java) và tên bảng database tương ứng.
- Đề xuất “bảng thuật ngữ chuẩn” VN/EN để thống nhất frontend-backend-docs.
- Quy ước đặt tên field/endpoint để giảm nhầm lẫn.

## 1) Bối cảnh và vấn đề đặt tên

Trong học vụ, có 3 khái niệm rất dễ bị gọi nhầm:
- **Môn học (catalog)**: một “định nghĩa môn” tương đối ổn định theo chương trình (code, credits, trọng số điểm…).
- **Lớp học phần (section / course offering)**: một “lớp mở theo học kỳ” cho một môn học, gắn giảng viên, lịch học, quota, cửa sổ đăng ký.
- **Lớp hành chính (admin class / cohort theo nghĩa intake)**: lớp sinh viên theo khóa/ngành, dùng cho quản lý hành chính.

Hiện tại dự án đang dùng:
- `Class` để chỉ **môn học (catalog)**.
- `Cohort` để chỉ **lớp học phần**.
- `AdminClass` để chỉ **lớp hành chính**.

Điều này chạy tốt về mặt kỹ thuật, nhưng có độ lệch so với nghĩa từ điển/usage phổ biến:
- “Class” thường bị hiểu là “lớp học” hoặc “lớp sinh viên”.
- “Cohort” thường bị hiểu là “khóa/đợt nhập học” (cohort 2026…).

## 2) Khái niệm hiện tại trong dự án (as-is)

### 2.1 Class (hiện tại) = Môn học (catalog)

- **Nghĩa dự án**: “môn học” (subject catalog) gồm: code, name, credits, trọng số điểm, số giờ…
- **Keywords**: class, course class, subject, credits, grade weights, processWeight, examWeight.
- **Entity / Table**:
  - Entity: `CourseClassEntity`
  - Table: `classes`
  - Controller: `ClassController` (`/api/v1/classes`)
- **Quan hệ**:
  - 1 `Class` có thể được mở nhiều `Cohort` theo các kỳ/học phần khác nhau.

### 2.2 Cohort (hiện tại) = Lớp học phần (course offering / section)

- **Nghĩa dự án**: một lớp học phần được mở theo học kỳ cho một môn học (class/catalog), có teacher, lịch học, quota, enrollment window, status vòng đời.
- **Keywords**: cohort, course offering, section, enrollment window, time slots, maxStudents/currentStudents, semester, teacher.
- **Entity / Table**:
  - Entity: `CohortEntity`
  - Table: `cohorts`
  - Controller: `CohortController` (`/api/v1/cohorts`)
- **Quan hệ**:
  - N-1 tới `classes` (field `class_id` trong DB, `clazz` trong entity).
  - N-1 tới `semesters` (field `semester_id`).
  - (Tuỳ chọn) N-1 tới `teachers` (field `teacher_id`).
  - 1-N với `cohort_time_slots`.
  - 1-N với `enrollments`.

### 2.3 AdminClass = Lớp hành chính

- **Nghĩa dự án**: lớp hành chính / lớp sinh viên theo khóa/ngành (intakeYear, program…).
- **Keywords**: admin class, class of students, intake year, program.
- **Entity / Table**:
  - Entity: `AdminClassEntity`
  - Table: `admin_classes`
  - Controller: `AdminClassController` (`/api/v1/admin-classes`)

### 2.4 Enrollment = Đăng ký học phần

- **Nghĩa dự án**: một sinh viên đăng ký vào một cohort (lớp học phần), kèm trạng thái và điểm.
- **Keywords**: enrollment, enrolled/dropped/completed/cancelled, scores, grade import.
- **Entity / Table**:
  - Entity: `EnrollmentEntity`
  - Table: `enrollments`
  - Controller: `EnrollmentController` (`/api/v1/enrollments`)
- **Quan hệ**:
  - N-1 tới `cohorts` qua `cohort_id`.
  - N-1 tới `students` qua `student_id`.

### 2.5 Attendance = Điểm danh

- **Attendance session**: buổi điểm danh cho một cohort theo ngày.
  - Entity: `AttendanceSessionEntity`
  - Table: `attendance_sessions`
  - Field: `cohort_id`, `session_date`, `periods`…
- **Attendance record**: điểm danh của một enrollment trong một session.
  - Entity: `AttendanceRecordEntity`
  - Table: `attendance_records`
  - Quan hệ: `session_id`, `enrollment_id`, `student_id`.
- API: `AttendanceController` (`/api/v1/attendance`)

### 2.6 Các bảng nền tảng khác (tham chiếu nhiều)

- Department: `DepartmentEntity` -> `departments` (`/api/v1/departments`)
- Semester: `SemesterEntity` -> `semesters` (`/api/v1/semesters`)
- Teacher: `TeacherEntity` -> `teachers` (`/api/v1/teachers`)
- Student: `StudentEntity` -> `students` (`/api/v1/students`)
- User/Auth: `UserEntity` -> `users`, `RoleEntity` -> `roles`, `UserRoleEntity` -> `user_roles`

## 3) Bảng mapping Entity ↔ DB ↔ API (as-is)

| Khái niệm (VN) | Term (EN, hiện tại) | Java entity | DB table | API base path |
|---|---|---|---|---|
| Môn học (catalog) | Class | `CourseClassEntity` | `classes` | `/api/v1/classes` |
| Lớp học phần | Cohort | `CohortEntity` | `cohorts` | `/api/v1/cohorts` |
| Lịch học lớp học phần | CohortTimeSlot | `CohortTimeSlotEntity` | `cohort_time_slots` | (thuộc cohorts) |
| Đăng ký học phần | Enrollment | `EnrollmentEntity` | `enrollments` | `/api/v1/enrollments` |
| Buổi điểm danh | AttendanceSession | `AttendanceSessionEntity` | `attendance_sessions` | `/api/v1/attendance` |
| Phiếu điểm danh | AttendanceRecord | `AttendanceRecordEntity` | `attendance_records` | `/api/v1/attendance` |
| Lớp hành chính | AdminClass | `AdminClassEntity` | `admin_classes` | `/api/v1/admin-classes` |
| Học kỳ | Semester | `SemesterEntity` | `semesters` | `/api/v1/semesters` |
| Khoa | Department | `DepartmentEntity` | `departments` | `/api/v1/departments` |
| Giảng viên | Teacher | `TeacherEntity` | `teachers` | `/api/v1/teachers` |
| Sinh viên | Student | `StudentEntity` | `students` | `/api/v1/students` |

## 4) Đề xuất “bảng thuật ngữ chuẩn” (to-be) để đúng nghĩa và ít nhầm

Mục tiêu: ai đọc docs/UI/endpoint/field cũng hiểu ngay “môn học” vs “lớp học phần” vs “lớp hành chính”.

### 4.1 Đề xuất thuật ngữ (VN/EN)

| Khái niệm (VN) | Đề xuất EN chuẩn | Ghi chú |
|---|---|---|
| Môn học (catalog) | Subject | Thuật ngữ phổ biến, đúng nghĩa. |
| Lớp học phần (mở theo kỳ) | CourseOffering hoặc Section | Chuẩn trong SIS. Nếu dùng Section thì rõ “lớp mở”. |
| Lớp hành chính | AdminClass hoặc StudentClass | Tránh dùng “Cohort” cho phần này nếu đã dùng elsewhere. |
| Đợt/khóa tuyển sinh | Cohort (optional) | Nếu thật sự có concept khóa/niên khóa thì dùng Cohort đúng nghĩa từ điển. |

### 4.2 Hai hướng triển khai (chọn 1)

**Hướng A (khuyến nghị, đổi naming chuẩn)**
- `Class` -> `Subject`
- `Cohort` -> `CourseOffering` (hoặc `Section`)
- `AdminClass` giữ nguyên

Ưu điểm: khớp từ điển/industry; giảm nhầm mạnh nhất.
Nhược điểm: là một refactor lớn (code + DB + frontend + docs).

**Hướng B (giữ naming hiện tại, chuẩn hoá bằng quy ước + docs)**
- Giữ `Class`/`Cohort` như hiện tại
- Tài liệu/Swagger/UI luôn ghi rõ:
  - “Class = Môn học (catalog)”
  - “Cohort = Lớp học phần”
  - “AdminClass = Lớp hành chính”

Ưu điểm: ít đụng code/DB.
Nhược điểm: vẫn trái nghĩa từ điển; người mới đọc dễ nhầm nếu bỏ qua mô tả.

## 5) Quy ước đặt tên field/endpoint để giảm nhầm lẫn

Mặc định đề xuất theo **Hướng A** (Subject + CourseOffering). Nếu giữ Hướng B, thay Subject->Class và CourseOffering->Cohort tương ứng.

### 5.1 Endpoint naming

- Catalog (môn học):
  - `GET /api/v1/subjects`
  - `GET /api/v1/subjects/{subjectId}`
- Lớp học phần:
  - `GET /api/v1/offerings` (hoặc `/api/v1/sections`)
  - `GET /api/v1/offerings/{offeringId}`
  - `GET /api/v1/offerings/{offeringId}/time-slots`
- Đăng ký:
  - `POST /api/v1/enrollments/self` với payload `{ offeringId }`
  - `GET /api/v1/enrollments/offering/{offeringId}`
- Điểm danh:
  - `POST /api/v1/attendance/sessions/open` với payload `{ offeringId, sessionDate, periods }`
  - `GET /api/v1/attendance/offerings/{offeringId}/sessions`

Quy tắc: endpoint nên dùng **thực thể “đúng nghĩa”** thay vì dùng từ chung chung như `courses`, `classes` vốn dễ bị hiểu nhầm.

### 5.2 Field naming trong DTO/JSON

Nguyên tắc:
- Field ID luôn theo mẫu `{noun}Id` với noun là khái niệm chuẩn.
- Object reference dùng noun trần: `subject`, `offering`, `adminClass`.

Ví dụ (đề xuất):
- `EnrollmentResponse`:
  - `offeringId` (hoặc `offering` object), tránh `courseId`/`classId` lẫn lộn.
- `OfferingResponse`:
  - `subjectId` + `subject` object
  - `semesterId` + `semester` object
  - `teacherId` + `teacher` object
- `AttendanceSessionResponse`:
  - `offeringId`

### 5.3 Tên biến trong frontend

Nguyên tắc:
- Danh sách catalog: `subjects`, filter: `subjectFilterId`, options: `subjectOptions`.
- Danh sách lớp học phần: `offerings`, route param: `offeringId`.
- Lớp hành chính: `adminClasses`, param: `adminClassId`.

Điều tối kỵ:
- Dùng `classId` để trỏ vào “lớp học phần”.
- Dùng `courseId` để trỏ vào “môn học catalog”.

## 6) Checklist tự kiểm tra khi thêm feature mới

- UI label có đúng VN không? (môn học vs lớp học phần vs lớp hành chính)
- Endpoint path có đúng concept không?
- DTO field name có đúng noun không?
- `...Id` có trỏ đúng table không? (subject_id/class_id vs cohort_id/offering_id)
- Tránh dùng từ “class” trong docs nếu đang nói về “lớp học phần” hoặc “lớp hành chính” (trừ khi có qualifier rõ ràng).

