# Đánh Giá Tác Động: Chức Năng Quản Lý Tiến Độ Học Tập

## 1. Tổng Quan Yêu Cầu
Triển khai chức năng theo dõi tiến độ học tập với phân quyền:
- **Admin**: Quản lý chương trình học và tiến độ của sinh viên.
- **Teacher**: Xem tiến độ của sinh viên thuộc lớp hành chính (Homeroom) mình phụ trách.
- **Student**: Xem tiến độ của chính mình.

## 2. Phân Tích Hiện Trạng (Current State)
- **StudentClass**: Hiện có trường `program` (String) và `advisorTeacher` (Teacher).
- **Subject**: Có trường `credits`.
- **Enrollment**: Lưu điểm số (`processScore`, `examScore`, `finalScore`).
- **Thiếu sót chính**:
    - Chưa có thực thể **Chương trình đào tạo (Curriculum/AcademicProgram)** chuẩn để định nghĩa danh sách môn học bắt buộc.
    - Chưa có logic tính toán **% hoàn thành** dựa trên tổng tín chỉ của chương trình.

## 3. Đánh Giá Tác Động (Impact Assessment)

### 3.1. Cơ sở dữ liệu (Database) - Mức độ: CAO (High)
Cần thiết kế lại cấu trúc để chuẩn hóa "Chương trình học".

**Các thay đổi cụ thể:**
1.  **Tạo bảng mới `academic_programs`**:
    - Thay thế trường `program` (String) trong `student_classes`.
    - Các trường: `id`, `name`, `code`, `total_credits` (tổng tín chỉ yêu cầu), `department_id`.
2.  **Tạo bảng mới `program_subjects` (Bảng nối)**:
    - Liên kết `academic_programs` và `subjects`.
    - Các trường: `program_id`, `subject_id`, `semester` (học kỳ dự kiến), `is_compulsory` (bắt buộc/tự chọn).
3.  **Cập nhật bảng `student_classes`**:
    - Thay đổi cột `program` (varchar) thành `program_id` (FK).

### 3.2. Business Logic (Backend) - Mức độ: TRUNG BÌNH (Medium)
Logic chủ yếu là thêm mới, ít ảnh hưởng đến code cũ (Open-Closed Principle).

1.  **Domain Models**:
    - Tạo model `AcademicProgram`, `ProgramSubject`.
    - Cập nhật `StudentClass` để link với `AcademicProgram`.
2.  **Service Logic mới**:
    - `AcademicProgramService`: CRUD chương trình học.
    - `StudentProgressService`: Tính toán tiến độ.
        - Công thức: `(Tổng tín chỉ môn đã qua) / (Tổng tín chỉ chương trình) * 100`.
        - Logic "Môn đã qua": `Enrollment.finalScore >= 4.0` (thang 10) hoặc quy tắc khác.
3.  **Security / Permissions**:
    - **Teacher Guard**: Kiểm tra `currentUser.teacherId == targetStudent.studentClass.advisorTeacherId`.
    - **Student Guard**: Kiểm tra `currentUser.studentId == targetStudent.id`.

### 3.3. API & Frontend - Mức độ: TRUNG BÌNH (Medium)
1.  **API**:
    - `GET /api/v1/academic-programs`: Admin quản lý.
    - `GET /api/v1/students/{id}/progress`: API lấy tiến độ (trả về % hoàn thành, danh sách môn đã học/chưa học).
2.  **Frontend**:
    - **Admin**: Trang quản lý khung chương trình (thêm môn vào chương trình).
    - **Teacher**: Tab "Tiến độ học tập" trong chi tiết sinh viên của lớp chủ nhiệm.
    - **Student**: Trang Dashboard hiển thị thanh tiến độ (Progress Bar) và bảng điểm tích lũy.

## 4. Kết Luận
- **Có phải thay đổi nhiều không?**:
    - **Database**: **Có**. Cần chuẩn hóa dữ liệu "Program" từ text sang bảng quan hệ để có thể tính toán chính xác.
    - **Logic**: **Vừa phải**. Chủ yếu là logic đọc và tính toán (Read-heavy), không ảnh hưởng luồng nghiệp vụ cốt lõi như Đăng ký tín chỉ hay Nhập điểm.
- **Rủi ro**:
    - Migration dữ liệu cũ (nếu đã có dữ liệu `program` dạng text không đồng nhất).
    - Định nghĩa "Hoàn thành" cần rõ ràng (bao gồm cả môn tự chọn/bắt buộc).
