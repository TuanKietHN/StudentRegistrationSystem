# Báo Cáo Hoàn Thành Giai Đoạn 2: Backend API & Security

## 1. DTOs (Data Transfer Objects)
Đã tạo đầy đủ các DTO cho các request và response liên quan:
- **`AcademicProgramCreateRequest`**: Request tạo chương trình mới.
- **`AcademicProgramUpdateRequest`**: Request cập nhật chương trình.
- **`AcademicProgramResponse`**: Response chi tiết chương trình.
- **`ProgramSubjectRequest`**: Request thêm môn học vào chương trình.
- **`ProgramSubjectResponse`**: Response chi tiết môn học trong chương trình.
- **`StudentProgressResponse`** & **`SubjectProgressDTO`**: Đã tạo ở giai đoạn 1.

## 2. Service Layer
- **`AcademicProgramService`**: Đã định nghĩa interface cho các thao tác CRUD chương trình.
- **`AcademicProgramServiceImpl`**: Đã triển khai logic:
    - CRUD chương trình học (Create, Update, Delete, Get).
    - Quản lý môn học trong chương trình (Thêm môn, Xóa môn).
    - Sử dụng `MapStruct` để map giữa Entity và DTO.
- **Mapper**: `AcademicProgramDTOMapper` đã được tạo để chuyển đổi dữ liệu.

## 3. Controller Layer (API Endpoints)
### 3.1. `AcademicProgramController` (`/api/v1/academic-programs`)
- `POST /`: Tạo chương trình (Admin).
- `PUT /{id}`: Cập nhật chương trình (Admin).
- `GET /{id}`: Xem chi tiết (Admin, Teacher, Student).
- `GET /`: Xem danh sách (Admin, Teacher).
- `DELETE /{id}`: Xóa chương trình (Admin).
- `POST /{id}/subjects`: Thêm môn vào chương trình (Admin).
- `DELETE /subjects/{subjectId}`: Xóa môn khỏi chương trình (Admin).
- `GET /{id}/subjects`: Xem danh sách môn (Admin, Teacher, Student).

### 3.2. `StudentProgressController` (`/api/v1/student-progress`)
- `GET /{studentId}`: Xem tiến độ học tập.
    - **Logic phân quyền (Security)**:
        - **Admin**: Xem được tất cả.
        - **Student**: Chỉ xem được của chính mình (kiểm tra `currentStudentId == targetStudentId`).
        - **Teacher**: Chỉ xem được của sinh viên thuộc lớp chủ nhiệm (kiểm tra `advisorTeacherId == currentTeacherId`).

## 4. Bảo Mật (Security)
- Sử dụng `@PreAuthorize` kết hợp với logic kiểm tra thủ công trong Controller để đảm bảo Data-level Security (quyền truy cập dữ liệu cụ thể).
- Đã xử lý các trường hợp ngoại lệ như `ResourceNotFoundException` và `AccessDeniedException`.

## 5. Kết Luận
Backend đã sẵn sàng để phục vụ các yêu cầu từ Frontend. API đã bao gồm đầy đủ chức năng quản lý chương trình và xem báo cáo tiến độ với cơ chế phân quyền chặt chẽ.

## 6. Bước Tiếp Theo (Giai Đoạn 3 - Frontend)
- Xây dựng giao diện Admin quản lý chương trình học.
- Xây dựng giao diện Dashboard cho Student xem tiến độ.
- Cập nhật giao diện Teacher để xem tiến độ của sinh viên lớp chủ nhiệm.
