# Refactoring Chức Năng Đăng Ký Học & Quản Lý Điểm

## Mục Tiêu (Objective)
- Tách bạch hoàn toàn chức năng "Ghi danh lớp học" (Course Registration) và "Quản lý Điểm" (Grading System) theo nguyên lý SRP (Single Responsibility Principle).
- Xóa bỏ việc hiển thị điểm số trên giao diện "Lớp đã đăng ký" của sinh viên.
- Rà soát và cấu trúc lại các Endpoint liên quan đến `Enrollment` hiện đang gộp chung xử lý cả Điểm và Trạng thái.

## Lên Kế Hoạch (Planning)
- [ ] Soạn thảo Kế hoạch triển khai (Implementation Plan) tách Frontend Component và Backend Controller.
- [ ] Chờ User phê duyệt (Review & Approve).

## Thực Thi (Execution)
### 1. Frontend Refactoring (Sinh viên)
- [ ] Gỡ bỏ cột `Điểm` khỏi màn hình quản lý lớp học đang đăng ký (`MyEnrollmentsView.vue`).
- [ ] Kiểm tra lại `CourseRegistrationView.vue` đảm bảo chức năng đăng ký, lọc theo học kỳ và xử lý trùng lịch hoạt động mượt mà.

### 2. Backend API Refactoring
- [ ] Tạo mới `SectionGradeController.java` để xử lý các Endpoint thao tác Điểm Số:
  - Import Excel điểm (`POST /grades/sections/{id}/import`)
  - Admin/Teacher cập nhật/chấm điểm (`PUT /grades/enrollments/{enrollmentId}`)
  - Admin/Teacher lấy danh sách sinh viên cùng điểm số trong lớp (`GET /grades/sections/{id}/enrollments`)
- [ ] Sửa đổi `EnrollmentController.java` về trạng thái "Thuần Đăng Ký Học":
  - Chỉ giữ lại Endpoint Thêm / Xóa Đăng ký (`POST`, `DELETE`).
  - Lấy danh sách đăng ký của sinh viên (`GET /me`, `GET /student/{id}`) mà không nhất thiết phải trả điểm (hoặc giữ nguyên DTO nhưng Frontend ẩn đi).
- [ ] Cập nhật logic phân quyền (Security Annotations) trong các Controller mới tách.

### 3. Frontend Refactoring (Giảng viên / Admin)
- [ ] Cập nhật gọi API trên `enrollment.service.ts` sang các endpoint mới ở `SectionGradeController` (Các hàm như `updateEnrollment`, `importGrades`, `getSectionEnrollments`).
- [ ] Đảm bảo màn hình `TeacherCourseEnrollmentsView.vue` và `AdminCourseEnrollmentsView.vue` vẫn nạp danh sách và cho phép chấm điểm bình thường.

## Kiểm tra và Nghiệm thu (Verification)
- [ ] Sinh viên: Không nhìn thấy Điểm khi đang trong kỳ học. Đăng ký / Hủy đăng ký bình thường.
- [ ] Giảng viên: Truy cập đúng danh sách điểm lớp mình dạy, chấm từng sinh viên và import bằng Excel trơn tru.
- [ ] Admin: Tương tự Giảng viên, được cấp toàn quyền thao tác.
