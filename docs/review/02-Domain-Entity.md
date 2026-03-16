# 02. Domain/Entity Review — Kiến trúc tầng domain & entity

> Cập nhật: 2026-03-11 — phản ánh trạng thái sau khi refactor layer leak.

## 1) Kiến trúc phân tầng (Clean Architecture)

Dự án áp dụng Clean Architecture theo mô hình:

```
api (Controllers + DTOs)
  ↓
application (Service interface + Impl)
  ↓
domain (Model + Repository interface)
  ↓
infrastructure/persistence (Entity + JPA Repository + Mapper)
```

Đánh giá: **Triển khai tốt và nhất quán** trên toàn bộ module (academic, auth, iam).

### 1.1. Ưu điểm kiến trúc

| Tiêu chí | Đánh giá |
|----------|----------|
| Tách biệt domain model vs JPA entity | ✅ Tất cả module |
| Repository interface ở domain layer | ✅ RepositoryImpl ở infrastructure |
| Mapper entity ↔ domain model | ✅ Đầy đủ (12 mapper cho academic + 1 user mapper) |
| Service interface + Impl | ✅ 12 service trong academic + 2 trong iam + 7 trong auth |
| DTO request/response riêng biệt | ✅ Mỗi entity có Create/Update/Response/Filter DTOs |

### 1.2. Điểm đã cải thiện (Đã sửa)

| # | Vấn đề | Trạng thái |
|---|--------|------------|
| 1 | **Layer leak trong `StudentProgressController`** (trực tiếp inject `StudentJpaRepository`, `TeacherJpaRepository`, `UserEntity`) | ✅ Đã sửa — chỉ còn inject `StudentProgressService`. Logic phân quyền chi tiết đã được chuyển vào service layer. |
| 2 | **Layer leak trong `AuthenticationService`** (import `StudentEntity`, `TeacherEntity`, `StudentJpaRepository`, `TeacherJpaRepository`) | ✅ Đã sửa — giờ dùng domain repositories `StudentRepository`/`TeacherRepository` và domain models `Student`/`Teacher`. |
| 3 | `CourseClass.java` trong domain/model | ❓ Cần xác nhận còn dùng không. |

## 2) Domain Models — Tổng quan

### 2.1. Auth Module

| Domain Model | Mô tả | Quan hệ |
|-------------|-------|---------|
| `User` | Tài khoản đăng nhập | roles (Set\<RoleType\>), login audit fields |

Đặc biệt: `User` domain model **không chứa** entity quan hệ JPA, chỉ dùng enum `RoleType`. Đây là đúng cách Clean Architecture.

Các field bảo mật đã có: `failedLoginAttempts`, `lockUntil`, `lastLoginAt`, `lastLoginIp`, `lastLoginUserAgent`.

### 2.2. Academic Module (13 domain models)

| Domain Model | Mô tả | Quan hệ quan trọng |
|-------------|-------|---------------------|
| `Department` | Khoa/bộ môn | parent (self-ref), headTeacher |
| `Teacher` | Giảng viên | user (User), department |
| `Student` | Sinh viên | user (User), department, studentClass |
| `Subject` | Môn học | departmentId (ID, không object ref) |
| `Semester` | Học kỳ | active, secondaryActive |
| `Cohort` | Niên khóa | startYear – endYear |
| `AcademicProgram` | Chương trình đào tạo | department |
| `ProgramSubject` | Môn trong CTĐT | programId, subject |
| `StudentClass` | Lớp hành chính | department, cohort, advisorTeacher, academicProgram |
| `Section` | Lớp học phần | subject, semester, teacher (User) |
| `SectionTimeSlot` | Lịch học | sectionId |
| `Enrollment` | Đăng ký học phần | section, student + scoring fields |
| `CourseClass` | ❓ Legacy | Cần xác nhận |

### 2.3. Enrollment — Rich domain model

Enrollment là domain model phức tạp nhất, hỗ trợ:
- Trạng thái: ENROLLED/DROPPED/COMPLETED
- Điểm: processScore / examScore / finalScore
- Khóa điểm: scoreLocked
- Phúc khảo: scoreOverridden + reason + timestamp + giá trị cũ (trước override)

→ **Thiết kế tốt** để hỗ trợ quy trình nhập điểm / phúc khảo thực tế.

## 3) Đánh giá domain model cho "quản lý khóa học nội bộ"

### ✅ Đã đủ

- CRUD Subject/Semester/Section (lớp học phần)
- Teacher/Student profiles liên kết 1:1 với User
- Department (hỗ trợ phân cấp)
- Enrollment + scoring + score locking + override
- Academic Program + Program Subjects
- Cohort + StudentClass (niên khóa + lớp hành chính)
- Section time slots (lịch học)
- Schedule API (lịch cá nhân)
- Student Progress tracking

### ⚠️ Chưa có nhưng có thể cần

| Tính năng | Ghi chú |
|-----------|---------|
| Attendance (điểm danh) | Enum `AttendanceStatus` đã có nhưng entity/table chưa implement |
| Prerequisite tracking | Không có bảng quan hệ prerequisite cho subjects |
| Room management | `section_time_slots.room` chỉ là VARCHAR text, không có bảng riêng |
| Notification system | Chưa có domain model cho thông báo |
| Gradebook chi tiết | Chỉ có process_score + exam_score, chưa chia nhỏ thành phần |

## 4) Kết luận

Domain model hiện tại **phù hợp tốt** cho "hệ thống quản lý khóa học nội bộ" mức trung bình. Kiến trúc Clean Architecture được triển khai nhất quán, có một vài layer leak cần refactor nhỏ (chủ yếu ở `StudentProgressController` và `AuthenticationService`).

Điểm mạnh nhất: separation of concerns rõ ràng giữa domain/entity, enrollment model phong phú, hỗ trợ score audit trail.
