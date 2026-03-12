# Kế Hoạch Tái Cấu Trúc Đăng Ký Học & Quản Lý Điểm (Course Registration & Grading Refactor)

## 1. Mục Tiêu
Quá trình đăng ký học hiện tại đang gắn chặt với chức năng quản lý điểm số, dẫn đến việc sinh viên thấy cột "Điểm" ngay trong lúc xem các lớp đã đăng ký (trong kỳ). Theo nguyên lý thiết kế, việc đăng ký ghi danh (Enrollment) và chấm điểm (Grading) cần được tách biệt.
Mục tiêu của lần tái cấu trúc này là:
1. Module "Đăng ký học" chỉ tập trung vào việc ghi danh, rút lớp, theo dõi lịch học.
2. Module "Quản lý điểm" phục vụ riêng cho Giáo viên / Admin để chấm điểm, khóa điểm, import excel.
3. Sinh viên chỉ coi điểm ở chức năng "Bảng Điểm / Tiến độ học tập" sau khi môn học kết thúc, không coi trên giao diện "Đăng ký học".

---

## 2. Chi Tiết Thay Đổi (Proposed Changes)

### 2.1. Backend
Chuyển đổi và tách tách rời (Decoupling) `EnrollmentController` và `EnrollmentService`.

#### Các Thêm Mới [NEW]
- **DTOs**:
  - `SectionGradeResponse.java`: Kế thừa hoặc chứa các thông tin của Enrollment kèm theo Điểm số (`processScore`, `examScore`, `finalScore`, `scoreLocked`, v.v.).
  - `GradeUpdateRequest.java`: Chứa các trường để cập nhật điểm.
- **Controller/Service**:
  - `SectionGradeController.java`: API Quản lý điểm theo lớp (`/api/v1/sections/{sectionId}/grades`).
  - `SectionGradeService.java` & `SectionGradeServiceImpl.java`: Chứa logic tính toán điểm, cập nhật điểm, import điểm từ Excel.

#### Các Sửa Đổi [MODIFY]
- **`EnrollmentResponse.java`**: Xóa đi các trường liên quan đến điểm số (`grade`, `processScore`, `examScore`, `finalScore`, `scoreLocked`, v.v.).
- **`EnrollmentUpdateRequest.java`**: Chỉ giữ lại trường cập nhật `status`.
- **`EnrollmentController.java` & `EnrollmentServiceImpl.java`**: Xóa các API/Method liên quan đến chấm điểm và import Excel. Chỉ giữ lại Đăng ký (`enroll`), Hủy (`cancel`), Lấy danh sách (`getStudentEnrollments`, `getMyEnrollments`).
- **`StudentProgressServiceImpl.java`** & **`StudentClassServiceImpl.java`**: Chỉnh sửa nhỏ nếu có tham chiếu đến các getter điểm cũ của `EnrollmentResponse` (thực tế các logic này gọi vào entity `Enrollment` trực tiếp, có thể không bị ảnh hưởng).

### 2.2. Frontend
#### Thêm Mới [NEW]
- `api/services/sectionGrade.service.ts`: API service client đóng gói gọi API cho phần Quản lý Điểm (`/api/v1/sections/`).

#### Sửa Đổi [MODIFY]
- **Sinh viên (`MyEnrollmentsView.vue`)**:
  - Xóa cột "Điểm".
  - Chỉnh lại giao diện cho phù hợp mục đích "chỉ xem trạng thái đăng ký và lịch học".
- **Giảng viên / Admin (`TeacherCourseEnrollmentsView.vue`, `AdminCourseEnrollmentsView.vue`)**:
  - Thay đổi service layer dùng để Load danh sách sinh viên: Từ `enrollmentService.getSectionEnrollments` sang `sectionGradeService.getSectionGrades`.
  - Thay đổi hàm lưu điểm (Save) từ `enrollmentService.updateEnrollment` sang `sectionGradeService.updateGrade`.
  - Thay đổi hàm import Excel (Import) sang `sectionGradeService.importGrades`.
- **`api/services/enrollment.service.ts`**:
  - Dọn dẹp/xóa các endpoint liên quan đến lấy điểm / import excel để giữ file gọn gàng.

---

## 3. Kế Hoạch Kiểm Tra (Verification Plan)
### Hệ thống (Codebase)
- Ứng dụng phải compile thành công (fix triệt để các thay đổi DTO).

### Kiểm thử thủ công (Manual Testing)
1. **Đăng nhập Sinh viên**:
   - Vào mục "Đăng ký học" -> Tab "Lớp đã đăng ký": Xác nhận KHÔNG CÒN cột điểm hiển thị.
   - Thử Đăng ký / Hủy đăng ký một môn học đang mở để đảm bảo chức năng Enroll hoạt động ổn.
2. **Đăng nhập Giảng viên**:
   - Truy cập vào lớp mình dạy -> Danh sách sinh viên: Kiểm tra dữ liệu Điểm hiện tại còn nguyên vẹn, đảm bảo đã chuyển sang dùng `SectionGradeController`.
   - Cập nhật điểm cho một sinh viên + Import file Excel hợp lệ xem hệ thống có lấy đúng quy trình mới không.
3. **Đăng nhập Admin**:
   - Vào mục Quản lý đào tạo -> Lớp học phần -> Danh sách sinh viên: Chỉnh sửa điểm (bị khóa) với lý do, kiểm tra xem luồng Admin Grade update có bị lỗi không.

---

> [!NOTE]
> Vui lòng phản hồi nếu bạn đồng ý với kế hoạch Refactor này để tôi bắt đầu thực hiện.
