# Kế Hoạch Triển Khai Giai Đoạn 1: Cấu Trúc Dữ Liệu & Logic Nền Tảng

## 1. Mục Tiêu Giai Đoạn 1
- Thiết lập nền tảng cơ sở dữ liệu (Database Foundation) cho việc quản lý chương trình học.
- Định nghĩa các thực thể (Entities) và Domain Models.
- Xác định rõ ràng các công thức tính toán điểm số và tiến độ học tập.

## 2. Thiết Kế Cơ Sở Dữ Liệu (Database Schema)

### 2.1. Bảng `academic_programs` (Chương trình đào tạo)
Lưu trữ thông tin khung chương trình học (VD: CNTT K15, Kinh Tế K16).

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK | Khóa chính |
| `code` | VARCHAR(50) | UNIQUE, NOT NULL | Mã chương trình (VD: IT_2024) |
| `name` | VARCHAR(255) | NOT NULL | Tên chương trình (VD: Kỹ thuật phần mềm 2024) |
| `department_id` | UUID | FK | Khoa quản lý |
| `total_credits` | INT | NOT NULL | Tổng số tín chỉ yêu cầu để tốt nghiệp |
| `description` | TEXT | | Mô tả chi tiết |
| `is_active` | BOOLEAN | DEFAULT TRUE | Trạng thái hoạt động |

### 2.2. Bảng `program_subjects` (Môn học trong chương trình)
Bảng trung gian định nghĩa các môn học thuộc một chương trình cụ thể.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK | Khóa chính |
| `program_id` | UUID | FK | Liên kết bảng `academic_programs` |
| `subject_id` | UUID | FK | Liên kết bảng `subjects` |
| `semester` | INT | NOT NULL | Học kỳ dự kiến (1-8) |
| `subject_type` | VARCHAR(20) | NOT NULL | Loại môn: `COMPULSORY` (Bắt buộc), `ELECTIVE` (Tự chọn) |
| `pass_score` | DOUBLE | DEFAULT 4.0 | Điểm tối thiểu để qua môn (thang 10) |

### 2.3. Cập nhật bảng `student_classes`
- **Thêm cột**: `program_id` (UUID, FK -> academic_programs).
- **Xử lý dữ liệu cũ**: Script migration để map dữ liệu từ cột `program` (String) cũ sang `program_id` mới nếu có thể, hoặc để null chờ Admin cập nhật.

## 3. Các Công Thức Tính Toán (Formulas)

### 3.1. Quy Tắc "Qua Môn" (Subject Pass Rule)
Một sinh viên được coi là **Hoàn thành (Passed)** một môn học khi:
- Có bản ghi `Enrollment` cho môn học đó.
- `Enrollment.finalScore` >= `ProgramSubject.pass_score` (Mặc định là **4.0** trên thang 10).

> **Lưu ý**: Hệ thống hiện tại lưu điểm thang 10 (`finalScore`). Chúng ta sẽ sử dụng thang 10 làm chuẩn để tính toán logic "Qua môn".

### 3.2. Công Thức Tính % Hoàn Thành Chương Trình (Program Completion)
Tiến độ được tính dựa trên **Tín chỉ tích lũy** (Credits Earned) so với **Tổng tín chỉ chương trình**.

$$
\text{Completion Rate} (\%) = \left( \frac{\sum \text{Credits of Passed Subjects}}{\text{Total Required Credits}} \right) \times 100
$$

**Chi tiết:**
1.  Lấy danh sách tất cả `Enrollment` của sinh viên.
2.  Lọc ra các môn có `finalScore >= 4.0`.
3.  Tính tổng tín chỉ (`Subject.credits`) của các môn đã qua này -> `earnedCredits`.
4.  Lấy `totalRequiredCredits` từ bảng `academic_programs`.
5.  Thực hiện phép chia và nhân 100.

### 3.3. Công Thức Quy Đổi Điểm (Grade Conversion)
Để hiển thị GPA hệ 4 (phổ biến trong tín chỉ), sử dụng bảng quy đổi sau (tham khảo quy chế đào tạo tín chỉ tiêu chuẩn):

| Điểm hệ 10 (Final Score) | Điểm Chữ (Letter Grade) | Điểm Hệ 4 (GPA Scale) | Đánh Giá |
| :--- | :--- | :--- | :--- |
| 8.5 - 10.0 | A | 4.0 | Giỏi (Excellent) |
| 7.0 - 8.4 | B | 3.0 | Khá (Good) |
| 5.5 - 6.9 | C | 2.0 | Trung bình (Average) |
| 4.0 - 5.4 | D | 1.0 | Trung bình yếu (Poor) |
| < 4.0 | F | 0.0 | Kém (Fail) |

**Công thức tính GPA hệ 4:**
$$
\text{GPA (4.0)} = \frac{\sum (\text{Subject Credits} \times \text{Converted Grade 4.0})}{\sum \text{Subject Credits}}
$$

## 4. Các Nhiệm Vụ Cụ Thể (Implementation Tasks)

### Task 1: Database Migration
- Tạo file migration Flyway `V{version}__create_academic_program_tables.sql`.
- Tạo bảng `academic_programs`, `program_subjects`.
- Alter table `student_classes`.

### Task 2: Domain Entities (Java)
- Tạo class `AcademicProgram.java` (Domain Model).
- Tạo class `ProgramSubject.java` (Domain Model).
- Tạo class `AcademicProgramEntity.java` (JPA Entity).
- Tạo class `ProgramSubjectEntity.java` (JPA Entity).
- Cập nhật `StudentClass.java` và `StudentClassEntity.java`.

### Task 3: Repository Layer
- Tạo `AcademicProgramRepository`.
- Tạo `ProgramSubjectRepository`.

### Task 4: Service Layer (Business Logic)
- **`AcademicProgramService`**: CRUD chương trình.
- **`GradeConversionService`**:
    - Method `convertToGPA4(double score10)`: chuyển đổi điểm 10 -> 4.
    - Method `getLetterGrade(double score10)`: lấy điểm chữ.
- **`StudentProgressService`**:
    - Method `calculateCompletionRate(UUID studentId)`: trả về %.
    - Method `calculateGPA(UUID studentId)`: trả về GPA hệ 4 và hệ 10.

## 5. API Response Specs (Dự kiến)
Dữ liệu trả về cho API xem tiến độ (`GET /api/v1/students/{id}/progress`):

```json
{
  "studentId": "...",
  "programName": "Kỹ thuật phần mềm K15",
  "progress": {
    "earnedCredits": 85,
    "totalCredits": 130,
    "completionRate": 65.38, // (85/130)*100
    "gpa10": 7.5,
    "gpa4": 3.12
  },
  "details": [
    {
      "subjectCode": "JAVA1",
      "subjectName": "Java Programming 1",
      "credits": 3,
      "score10": 8.0,
      "grade4": 3.0,
      "letterGrade": "B",
      "status": "PASSED"
    },
    {
      "subjectCode": "MATH2",
      "subjectName": "Calculus 2",
      "credits": 3,
      "score10": null,
      "status": "NOT_STARTED"
    }
  ]
}
```
