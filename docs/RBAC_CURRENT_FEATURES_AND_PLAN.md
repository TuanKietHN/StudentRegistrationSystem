# RBAC (Phân quyền) — Hiện trạng & Kế hoạch chuyển sang phân quyền động

## 1. Hiện trạng xác thực (Authentication)

### Backend
- Cơ chế: OAuth2 Resource Server (JWT) + Stateless.
- JWT:
  - HS256 (shared secret) cấu hình trong [JwtConfig.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/common/config/JwtConfig.java).
  - Access token chứa claim `scope` (chuỗi authorities cách nhau bằng khoảng trắng) do [JwtProvider](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/common/security/JwtProvider.java) phát hành.
  - Resource server đọc claim `scope` và convert thành `GrantedAuthority` tại [SecurityConfig.jwtAuthenticationConverter()](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/common/config/SecurityConfig.java#L54-L63).
- Refresh token:
  - Refresh token là opaque UUID và được quản lý theo “session” trên Redis (kèm rotation + reuse detection).
  - Cookie HttpOnly cho refresh token + deviceId (phục vụ 1 device ↔ 1 session) tại [AuthCookieService](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/auth/application/AuthCookieService.java).
  - Logic issue/rotate/revoke nằm trong [AuthSessionService](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/auth/application/AuthSessionService.java) và implementation Redis tại [RedisSessionRepository](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/auth/infrastructure/persistence/repository/RedisSessionRepository.java).
- Public endpoints (permitAll) trong [SecurityConfig](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/common/config/SecurityConfig.java): `/api/v1/auth/login|register|refresh|forgot-password|reset-password` + swagger; còn lại yêu cầu `authenticated()`.

### Frontend
- Axios `withCredentials: true` để gửi cookie refresh; attach `Authorization: Bearer <accessToken>` cho mọi request tại [axios.ts](file:///c:/Users/Admin/Downloads/Repo/cms/frontend/src/api/axios.ts).
- Interceptor xử lý 401: gọi refresh rồi retry request (queue đơn giản bằng `refreshPromise`).
- Router guards chặn route theo `requiresAuth`, `roles`, `activeRoleRequired` tại [guards.ts](file:///c:/Users/Admin/Downloads/Repo/cms/frontend/src/router/guards.ts).

## 2. Hiện trạng phân quyền (Authorization)

### 2.1. Backend đang “hardcode” ở mức nào?

Kết luận ngắn gọn:
- **Role của user không hardcode trong code** (roles được lưu/đọc từ DB, gán qua bảng `user_roles` ↔ `roles`).
- Nhưng **chính sách truy cập (policy) đang hardcode trong code** thông qua `@PreAuthorize("hasRole('ADMIN')")`, `hasAnyRole(...)`, và một vài chỗ so sánh string `"ROLE_ADMIN"` trực tiếp.

Chi tiết:
- DB đã có sẵn các bảng RBAC: `roles`, `permissions`, `role_permissions`, `user_roles` (xem [V1__init_schema.sql](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/resources/db/migration/V1__init_schema.sql#L249-L302)).
- Code backend hiện tại:
  - Đã map **user ↔ role** bằng JPA entity [RoleEntity/UserRoleEntity](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/auth/infrastructure/persistence/entity/RoleEntity.java) và `UserRepositoryImpl` (tự upsert role nếu thiếu).
  - **Chưa dùng bảng `permissions`/`role_permissions` trong code** (không thấy `PermissionEntity`/repository/service tương ứng).
  - Nhiều API bị khóa theo role trong annotation:
    - `@PreAuthorize("hasRole('ADMIN')")` ví dụ [UserController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/iam/api/UserController.java#L18-L23) và nhiều controller academic.
    - `@PreAuthorize("hasRole('STUDENT')")`, `hasAnyRole('ADMIN','TEACHER')` trong [EnrollmentController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/EnrollmentController.java).
  - Một vài endpoint “READ” **không có `@PreAuthorize`**, nên hiện tại chỉ cần `authenticated()` là truy cập được (ví dụ `GET /api/v1/departments`, `GET /api/v1/classes`, `GET /api/v1/subjects`, `GET /api/v1/semesters`, `GET /api/v1/cohorts`, `GET /api/v1/sections`).

### 2.2. Frontend kiểm soát role hiện tại
- Router meta đã chặn theo role/activeRole trên route prefix `/admin`, `/teacher`, `/student` tại [router/index.ts](file:///c:/Users/Admin/Downloads/Repo/cms/frontend/src/router/index.ts).
- Một số view vẫn tự kiểm tra role bằng cách `split(',').includes('ADMIN')` (không normalize `ROLE_`), dễ lệch môi trường nếu backend trả `ROLE_ADMIN` thay vì `ADMIN`.

## 3. Chức năng hiện có theo từng role (từ API + UI)

### 3.1. ADMIN

**IAM**
- Quản lý người dùng (CRUD + avatar): `GET/POST/PUT/DELETE /api/v1/users` và `POST /api/v1/users/{id}/avatar` (Admin only) — [UserController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/iam/api/UserController.java).

**Academic (danh mục & tổ chức học vụ)**
- Khoa/Department: CRUD (Admin) + list/get (Authenticated) — [DepartmentController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/DepartmentController.java).
- Môn học catalog/Class: CRUD (Admin) + list/get (Authenticated) — [ClassController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/ClassController.java).
- Môn học/Subject: CRUD (Admin) + list/get (Authenticated) — [SubjectController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/SubjectController.java).
- Học kỳ/Semester: CRUD (Admin) + list/get/active (Authenticated) — [SemesterController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/SemesterController.java).
- Lớp học phần/Cohort: CRUD (Admin) + list/get (Authenticated) — [CohortController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/CohortController.java).
- Lớp mở theo kỳ/Section: CRUD/merge/cancel/time-slots (Admin) + list/get/time-slots (Authenticated) — [SectionController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/SectionController.java).
- Hồ sơ giảng viên/Teacher: CRUD (Admin) + list/get/getByUserId (Authenticated) — [TeacherController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/TeacherController.java).
- Hồ sơ sinh viên/Student: CRUD + list/get/getByUserId (Admin only) — [StudentController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/StudentController.java).
- Lớp hành chính/StudentClass: CRUD (Admin) + list/get/listStudents (Admin hoặc Teacher) — [StudentClassController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/StudentClassController.java).

**Enrollment/Đăng ký học phần**
- Admin tạo đăng ký: `POST /api/v1/enrollments` (Admin) — [EnrollmentController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/EnrollmentController.java#L26-L32).
- Admin sửa/xóa đăng ký: `PUT /api/v1/enrollments/{id}` (Admin/Teacher), `DELETE /api/v1/enrollments/{id}` (Admin/Student) — [EnrollmentController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/EnrollmentController.java#L42-L62).
- Admin xem đăng ký theo sinh viên/lớp học phần: `GET /api/v1/enrollments/student/{studentId}`, `GET /api/v1/enrollments/sections/{sectionId}` (Admin/Teacher) — [EnrollmentController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/EnrollmentController.java#L64-L88).
- Import điểm theo section: `POST /api/v1/enrollments/sections/{sectionId}/grades/import` (Admin/Teacher) — [EnrollmentController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/EnrollmentController.java#L90-L102).

**UI**
- Nhóm màn hình `/admin/*`: Users, Departments, Teachers, Semesters, Subjects, Sections, StudentClasses, Enrollments… (xem `frontend/src/views/admin` + các list view) và được chặn bởi router meta roles `['ADMIN']`.

### 3.2. TEACHER

**Academic**
- Xem lớp hành chính (student class) + danh sách sinh viên theo lớp: `GET /api/v1/student-classes`, `GET /api/v1/student-classes/{id}/students` (Admin/Teacher) — [StudentClassController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/StudentClassController.java).
- Xem danh sách section (có logic lọc theo teacher nếu role TEACHER) — [SectionController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/SectionController.java#L30-L70).

**Enrollment**
- Cập nhật thông tin đăng ký/điểm: `PUT /api/v1/enrollments/{id}` (Admin/Teacher) — [EnrollmentController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/EnrollmentController.java#L42-L53).
- Xem enrollment theo section + import điểm: `GET /api/v1/enrollments/sections/{sectionId}`, `POST /api/v1/enrollments/sections/{sectionId}/grades/import` (Admin/Teacher) — [EnrollmentController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/EnrollmentController.java#L80-L102).

**UI**
- Nhóm màn hình `/teacher/*`: TeacherHome, TeacherCourses, TeacherCourseEnrollments, TeacherAdminClasses… (xem `frontend/src/views/teacher`) và bị chặn bởi router meta roles `['TEACHER']`.

### 3.3. STUDENT

**Enrollment**
- Tự đăng ký học phần: `POST /api/v1/enrollments/self` (Student) — [EnrollmentController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/EnrollmentController.java#L34-L40).
- Xem danh sách học phần của tôi: `GET /api/v1/enrollments/me` (Student) — [EnrollmentController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/EnrollmentController.java#L72-L78).
- Hủy đăng ký: `DELETE /api/v1/enrollments/{id}` (Admin/Student) — [EnrollmentController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/api/EnrollmentController.java#L55-L62).

**UI**
- Nhóm màn hình `/student/*`: CourseRegistration, MyEnrollments, StudentHome… (xem `frontend/src/views/student`) và bị chặn bởi router meta roles `['STUDENT']`.

### 3.4. Chức năng chung (mọi user đã đăng nhập)
- Xem “me” + authorities hiện tại: `GET /api/v1/auth/me` — [AuthController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/auth/api/AuthController.java#L56-L65).
- Quản lý session đăng nhập (theo thiết bị): `GET/DELETE /api/v1/auth/sessions...` — [AuthSessionController](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/auth/api/AuthSessionController.java).
- Các API “read catalog” đang chỉ yêu cầu `authenticated()` (chưa chặn theo role): departments/classes/subjects/semesters/cohorts/sections.

## 4. Kế hoạch chuyển sang phân quyền động (phù hợp DB hiện tại)

Mục tiêu: thay vì “gắn cứng” policy theo `ROLE_*`, chuyển sang mô hình:
- **Role**: nhóm quyền (dynamic trong DB).
- **Permission**: quyền thao tác cụ thể (`RESOURCE:ACTION`) và được gán cho role qua `role_permissions` (dynamic trong DB).
- **User**: có nhiều role (qua `user_roles`) và được hưởng permission hợp nhất.

### 4.1. Thiết kế đề xuất (khuyến nghị)

**A. Cơ chế cấp quyền runtime**
- Khi login/refresh:
  - Load roles của user từ `user_roles`/`roles`.
  - Load permissions theo role từ `role_permissions`/`permissions`.
  - Tạo danh sách authorities gồm:
    - `ROLE_*` (để giữ tương thích ngược trong thời gian chuyển đổi)
    - `COURSE:READ`, `ENROLLMENT:UPDATE`, ... (permission)
  - Nhét toàn bộ vào claim `scope` của JWT (đúng với converter hiện tại).

**B. Chính sách truy cập ở Controller**
- Chuyển dần từ:
  - `@PreAuthorize("hasRole('ADMIN')")`
  - sang `@PreAuthorize("hasAuthority('USER:READ')")`, `@PreAuthorize("hasAuthority('DEPARTMENT:CREATE')")`, …
- Đặt “permission name” thành hằng số dùng chung để tránh typo (ví dụ `Permissions.USER_READ = "USER:READ"`), và thống nhất resource/action.

**C. Quản trị phân quyền**
- Bổ sung API quản trị (Admin-only) để:
  - CRUD role (bảng `roles`)
  - CRUD permission (bảng `permissions`)
  - Gán permission cho role (bảng `role_permissions`)
  - Gán role cho user (bảng `user_roles`)
- Frontend thêm UI “Role & Permission Management” trong `/admin`.

### 4.2. Những việc bắt buộc bổ sung vào DB

DB hiện có mới seed một tập permission mẫu cho `COURSE/USER/ENROLLMENT` (xem [V1__init_schema.sql](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/resources/db/migration/V1__init_schema.sql#L506-L542)).

Để cover toàn bộ API hiện có, cần bổ sung permissions tối thiểu cho các resource:
- `DEPARTMENT`, `SUBJECT`, `SEMESTER`, `CLASS`, `COHORT`, `SECTION`, `STUDENT_CLASS`, `TEACHER`, `STUDENT`, `GRADE` (nếu tách riêng), …

### 4.3. Lộ trình triển khai (không phá vỡ hệ thống)

**Phase 0 — Khóa hiện trạng bằng tài liệu**
- Chốt matrix chức năng theo role (mục 3) + danh mục permissions cần có.

**Phase 1 — Bật “permission authorities” trong JWT**
- Đã thêm entity cho `permissions` + `role_permissions` và query permission theo role.
- Đã đưa permission vào JWT `scope` khi login/refresh (kèm `ROLE_*` để tương thích ngược).
- Giữ nguyên các `@PreAuthorize(hasRole...)` để không ảnh hưởng chạy thực tế.

**Phase 2 — Chuyển dần policy sang permission**
- Đã bắt đầu chuyển đổi:
  - `UserController`: `hasAuthority('USER:*')`.
  - Academic: `DepartmentController`, `ClassController`, `SubjectController`, `SemesterController`, `CohortController`, `TeacherController`, `StudentController`, `StudentClassController`, `SectionController`, `EnrollmentController`.
- Tiếp tục đổi từng controller endpoint từ `hasRole(...)` sang `hasAuthority('X:Y')`.
- Đồng thời cập nhật seed/migration để role hiện tại có đủ permissions tương ứng.

**Phase 3 — Quản trị RBAC từ UI**
- API + UI quản trị role/permission/mapping.
- Audit event cho các thay đổi nhạy cảm (ai đổi role/permission, lúc nào, trước/sau).

**Phase 4 (tuỳ chọn) — Fully dynamic endpoint-policy**
- Mapping (HTTP method + path pattern) ↔ permission nằm trong DB và enforce bằng custom `AuthorizationManager`/filter.
- Ưu: thay policy mà không redeploy; Nhược: phức tạp hơn, cần kỷ luật naming và cache/invalidations.

## 5. Gaps/Rủi ro cần xử lý sớm

- Một số endpoint “read catalog” hiện không có `@PreAuthorize` theo role/permission → cần quyết định rõ “ai được xem” (student/teacher/admin) và enforce đồng nhất.
- Frontend có các check role “không normalize” → dễ lệch hành vi giữa môi trường.
- Một số tài liệu trong repo có thể bị lệch so với code (ví dụ tài liệu mô tả `@ElementCollection` cho roles nhưng code hiện map qua `user_roles`/`roles`) → nên chỉnh lại để tránh hiểu nhầm khi onboard.
