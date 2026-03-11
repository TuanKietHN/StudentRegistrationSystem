# 03. Chức năng hiện có, đánh giá chất lượng & khoảng trống

> Cập nhật: 2026-03-11

## 1) Chức năng hiện có — Backend API

### 1.1. Auth Module (7 services, 2 controllers)

| Endpoint | Chức năng | Trạng thái |
|----------|-----------|-----------|
| `POST /api/v1/auth/login` | Đăng nhập (JWT + Refresh Token cookie) | ✅ |
| `POST /api/v1/auth/register` | Đăng ký tài khoản | ✅ |
| `POST /api/v1/auth/refresh` | Làm mới access token | ✅ |
| `POST /api/v1/auth/logout` | Đăng xuất + blacklist JWT | ✅ |
| `POST /api/v1/auth/forgot-password` | Quên mật khẩu (qua email) | ✅ |
| `POST /api/v1/auth/reset-password` | Đặt lại mật khẩu | ✅ |
| `GET /api/v1/auth/me` | Thông tin phiên hiện tại | ✅ |
| `GET /api/v1/auth/sessions` | Quản lý sessions | ✅ |

Tính năng bảo mật nâng cao đã implement:
- ✅ JWT blacklisting (Redis) khi logout
- ✅ Refresh token rotation (detect reuse → lock account)
- ✅ Rate limiting cho login failures
- ✅ Account locking sau N lần thất bại
- ✅ Auth audit logging (ghi lại mọi sự kiện auth)
- ✅ Device ID tracking qua cookie
- ✅ CSRF protection (SPA-compatible)
- ✅ XSS filter + CSP headers
- ✅ Session management qua Redis

### 1.2. IAM Module (User Management)

| Endpoint | Chức năng | Phân quyền |
|----------|-----------|------------|
| `GET /api/v1/users` | Danh sách user (search + filter) | ADMIN |
| `GET /api/v1/users/{id}` | Chi tiết user | ADMIN |
| `POST /api/v1/users` | Tạo user | ADMIN |
| `PUT /api/v1/users/{id}` | Cập nhật user | ADMIN |
| `DELETE /api/v1/users/{id}` | Xóa user | ADMIN |
| `POST /api/v1/users/{id}/avatar` | Upload avatar (MinIO) | ADMIN |

### 1.3. Academic Module (12 controllers, 12 services)

| Resource | CRUD | Tính năng đặc biệt | Phân quyền |
|----------|------|---------------------|------------|
| **Department** | ✅ Full | Hỗ trợ parent (phân cấp) | Permission-based |
| **Teacher** | ✅ Full | Profile liên kết User 1:1 | Permission-based |
| **Student** | ✅ Full | Profile liên kết User 1:1 + StudentClass | Permission-based |
| **Subject** | ✅ Full | Credits, grade weights | Permission-based |
| **Semester** | ✅ Full | Active/secondary_active | Permission-based |
| **Cohort** | ✅ Full | Cancel, merge | Permission-based |
| **AcademicProgram** | ✅ Full | Program ↔ Subject mapping | Permission-based |
| **StudentClass** | ✅ Full | Lớp hành chính | Permission-based |
| **Section** | ✅ Full | Cancel, merge, time slots | Permission-based |
| **Enrollment** | ✅ Full | Self-enroll, grades, import Excel | Permission + Role |
| **Schedule** | ✅ Read | Lịch cá nhân (student/teacher) | Role-based |
| **StudentProgress** | ✅ Read | Tiến độ học tập theo CTĐT | Permission-based (fine-grained) |

Tính năng enrollment nổi bật:
- ✅ Sinh viên tự đăng ký lớp học phần (`POST /enrollments/self`)
- ✅ Kiểm tra thời gian đăng ký (enrollment start/end date)
- ✅ Kiểm tra sĩ số (maxStudents)
- ✅ Import điểm từ Excel (`POST /enrollments/sections/{id}/grades/import`)
- ✅ Score locking (teacher nhập 1 lần, admin phúc khảo)
- ✅ Score override với audit trail

### 1.4. Infrastructure

| Component | Công nghệ | Trạng thái |
|-----------|-----------|-----------|
| Database | PostgreSQL | ✅ |
| Cache | Redis | ✅ |
| Message Queue | RabbitMQ (email) | ✅ |
| File Storage | MinIO | ✅ |
| API Docs | Swagger/OpenAPI | ✅ |
| Migration | Flyway (auto-config) | ✅ |

## 2) Chức năng hiện có — Frontend

### 2.1. Tổng quan (28 views, Vite + Vue 3 + Vuetify + TypeScript)

| Nhóm | Views | Mô tả |
|------|-------|-------|
| Auth | 4 views | Login, Register, ForgotPassword, ResetPassword |
| General | 3 views | Home, RootRedirect, NotFound, Schedule |
| Academic (admin) | 7 views | Department, Subject, Semester, Teacher, CourseList, AdminClasses, Program |
| Student | 3 views | Home, CourseRegistration, MyEnrollments |
| Teacher | 6 views | Home, Courses, Enrollments, AdminClasses, Students, Grades |
| IAM | 1 view | UserList |

Frontend có 15 API services tương ứng với backend, dùng Axios interceptors.

### 2.2. Đánh giá frontend

| Tiêu chí | Đánh giá |
|----------|----------|
| Tech stack | ✅ Hiện đại (Vue 3 + Vite + TS + Vuetify) |
| API integration | ✅ 15 services cover toàn bộ backend |
| Role-based views | ✅ Student/Teacher/Admin views tách biệt |
| Schedule view | ✅ Xem lịch cá nhân |
| Student progress | ✅ Xem tiến độ theo CTĐT |

## 3) Phân quyền — Đánh giá chi tiết

### 3.1. Mô hình RBAC hiện tại

```
User ← M:N → Role (ADMIN, TEACHER, STUDENT)
Role ← M:N → Permission (RESOURCE:ACTION)
```

- 48+ permissions, 12 resources (USER, DEPARTMENT, SUBJECT, SEMESTER, COHORT, SECTION, TEACHER, STUDENT, STUDENT_CLASS, ENROLLMENT, ACADEMIC_PROGRAM, STUDENT_PROGRESS)
- Controller dùng `@PreAuthorize("hasAuthority('RESOURCE:ACTION')")` kết hợp `hasRole()`
- AuthenticationService build scope = roles + permissions vào JWT

### 3.2. Vấn đề nhỏ

| # | Vấn đề | Chi tiết |
|---|--------|----------|
| 1 | Phân quyền chưa nhất quán | Một số controller dùng `hasRole()`, một số dùng `hasAuthority()`, một số kết hợp cả hai. Nên chuẩn hoá. |
| 2 | Role check inline trong controller | Nhiều controller tự check `isAdmin/isTeacher` bằng code thay vì dùng `@PreAuthorize` + SpEL. Gây duplicate logic. |
| 3 | `ScheduleController` dùng `hasAnyRole()` thay vì permission | Không nhất quán với các controller khác dùng hasAuthority. |

## 4) Chất lượng code — Đánh giá tổng thể

### 4.1. Điểm mạnh

| # | Điểm mạnh | Mô tả |
|---|-----------|-------|
| 1 | Clean Architecture | Tách biệt rõ api → application → domain → infrastructure |
| 2 | Comprehensive RBAC | Permission-based, không chỉ role-based |
| 3 | Auth security | JWT blacklist, token rotation, rate limit, audit log, account lock |
| 4 | DataSeeder phong phú | 1230 dòng, tạo ~400 students, 18 sections, enrollments, grades |
| 5 | Score audit trail | Override + lý do + timestamp + giá trị cũ |
| 6 | Section lifecycle | OPEN/CLOSED/CANCELED/MERGED + cancel/merge API |
| 7 | Swagger/OpenAPI | Tất cả endpoints có documentation |
| 8 | Global exception handler | Xử lý đầy đủ: Business, NotFound, Validation, Auth, Access Denied |

### 4.2. Điểm cần cải thiện

| # | Vấn đề | Mức độ | Gợi ý |
|---|--------|--------|-------|
| 1 | Layer leak (Controller → JPA entity) | 🟡 Trung bình | Refactor `StudentProgressController`, `AuthenticationService` |
| 2 | `auth_audit_events` thiếu migration | 🔴 Cao | Tạo migration V4 |
| 3 | Test coverage chưa rõ | 🟡 Trung bình | Chưa thấy unit test/integration test cho services |
| 4 | isAdmin/isTeacher check duplicate | 🟡 Trung bình | Extract thành utility hoặc custom annotation |
| 5 | `GlobalExceptionHandler` catch `RuntimeException` quá rộng | 🟡 Trung bình | Có thể nuốt unexpected errors; nên log stack trace |
| 6 | `CourseClass.java` legacy file | 🟢 Thấp | Xác nhận và xóa nếu không dùng |
| 7 | `Course*` DTOs (legacy) | 🟢 Thấp | CourseCreateRequest, CourseFilterRequest... có thể legacy từ trước Section refactor |
| 8 | `PasswordEncoder` bean trong `SecurityConfig` | 🟢 Thấp | Có thể tách thành `@Bean` riêng để tránh circular dependency |

## 5) Khoảng trống — VS "quản lý khóa học nội bộ" hoàn chỉnh

### 5.1. Đã có → hoàn chỉnh

- ✅ Auth + Security (advanced level)
- ✅ User management (CRUD + avatar)
- ✅ Department/Teacher/Student profiles
- ✅ Subject/Semester/Section (lớp học phần) CRUD
- ✅ Enrollment (self-enroll + admin enroll + scoring + import Excel)
- ✅ Academic Program (CTĐT) + Student Progress tracking
- ✅ Schedule (lịch cá nhân)
- ✅ Cohort + StudentClass (niên khóa + lớp hành chính)

### 5.2. Chưa có → cân nhắc bổ sung

| # | Tính năng | Ưu tiên | Ghi chú |
|---|-----------|---------|---------|
| 1 | Attendance (điểm danh) | 🟡 | Enum đã có, cần entity + table + API |
| 2 | Notification system | 🟡 | Email qua RabbitMQ đã sẵn, cần in-app notification |
| 3 | Prerequisite enforcement | 🟢 | Kiểm tra môn tiên quyết khi đăng ký |
| 4 | Room/building management | 🟢 | section_time_slots.room chỉ lưu text |
| 5 | Gradebook chi tiết | 🟢 | Chia nhỏ process_score thành các cột thành phần |
| 6 | Export reports (PDF/Excel) | 🟡 | Bảng điểm, DSSV, báo cáo |
| 7 | Dashboard & statistics | 🟡 | Thống kê theo khoa/học kỳ/giảng viên |
| 8 | Audit log nghiệp vụ | 🟢 | Ai đổi điểm, ai hủy enrollment... (auth audit có, business audit chưa) |

## 6) Kết luận

Dự án đã phát triển **đáng kể** so với lần review trước:

| So sánh | Trước | Hiện tại |
|---------|-------|----------|
| Java files | ~50 | 70+ |
| Controllers | 4 | 14 |
| Services | 4 | 21+ |
| Domain models | 4 | 13+ |
| Frontend views | 2 | 28 |
| API services (FE) | 0 | 15 |
| Auth features | JWT cơ bản | JWT + blacklist + session + audit + rate limit |
| RBAC | Role only | 48+ permissions |

Backend đạt mức **production-ready** cho hệ thống quản lý khóa học nội bộ trung bình. Cần bổ sung: unit test, fix migration cho `auth_audit_events`, refactor một vài layer leak nhỏ.
