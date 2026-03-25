# Báo Cáo Hoàn Thành Giai Đoạn 1: Nền Tảng & Logic Tính Toán

## 1. Cơ Sở Dữ Liệu (Database Foundation)
Đã thiết lập cấu trúc bảng mới thông qua migration script `V24__create_academic_programs.sql`:
- **`academic_programs`**: Lưu trữ khung chương trình đào tạo.
- **`program_subjects`**: Định nghĩa môn học trong chương trình và điểm đạt yêu cầu.
- **`student_classes`**: Thêm liên kết `program_id` để gắn lớp với chương trình.

## 2. Domain Models & Entities
Đã tạo các lớp Java tương ứng theo kiến trúc Clean Architecture:
- **Domain**: `AcademicProgram`, `ProgramSubject`.
- **Entity**: `AcademicProgramEntity`, `ProgramSubjectEntity`.
- **Cập nhật**: `StudentClass` đã có thêm trường `academicProgram`.

## 3. Logic Tính Toán (Core Calculation Logic)
Logic đã được cài đặt trong `StudentProgressServiceImpl`.

### 3.1. Quy Tắc Qua Môn (Subject Pass Rule)
- **Công thức**: `finalScore >= passScore`
- **Mặc định**: `passScore = 4.0` (thang 10).
- **Cấu hình**: Có thể tùy chỉnh `passScore` cho từng môn trong `program_subjects`.

### 3.2. Tính % Hoàn Thành (Completion Rate)
- **Công thức**:
  $$ \text{Progress} = \left( \frac{\text{Tổng tín chỉ các môn ĐÃ QUA}}{\text{Tổng tín chỉ yêu cầu của chương trình}} \right) \times 100 $$
- **Lưu ý**: Chỉ tính tín chỉ của các môn có trạng thái `PASSED`.

### 3.3. Quy Đổi Điểm GPA (Grade Conversion)
Đã cài đặt hàm chuyển đổi chuẩn:

| Điểm hệ 10 | Điểm Chữ | Điểm Hệ 4 |
| :--- | :--- | :--- |
| 8.5 - 10.0 | A | 4.0 |
| 7.0 - 8.4 | B | 3.0 |
| 5.5 - 6.9 | C | 2.0 |
| 4.0 - 5.4 | D | 1.0 |
| < 4.0 | F | 0.0 |

- **GPA (10)**: Trung bình cộng gia quyền theo tín chỉ (hệ 10).
- **GPA (4)**: Trung bình cộng gia quyền theo tín chỉ (sau khi quy đổi sang hệ 4).

## 4. Các File Đã Tạo
- **Migration**: `src/main/resources/db/migration/V24__create_academic_programs.sql`
- **Domain**: `AcademicProgram.java`, `ProgramSubject.java`
- **Entity**: `AcademicProgramEntity.java`, `ProgramSubjectEntity.java`
- **Repository**: `AcademicProgramRepository`, `ProgramSubjectRepository` (kèm Impl và JpaRepo)
- **Service**: `StudentProgressServiceImpl.java` (chứa logic tính toán chính)
- **DTO**: `StudentProgressResponse.java`, `SubjectProgressDTO.java`

## 5. Bước Tiếp Theo (Giai Đoạn 2)
- Triển khai API Controller để expose dữ liệu này ra Frontend.
- Xây dựng giao diện Admin để nhập liệu Chương trình học (CRUD).
- Xây dựng giao diện xem tiến độ cho Student và Teacher.
