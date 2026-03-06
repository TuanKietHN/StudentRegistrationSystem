# Kế hoạch Triển khai Dự án Course Management System (CMS)

## 1. Tổng quan Dự án
*   **Tên dự án**: Course Management System (CMS)
*   **Mục tiêu**: Xây dựng hệ thống quản lý khóa học nội bộ hiện đại (không bao gồm LMS/khóa học online).
*   **Kiến trúc**: Modular Monolith (Clean Architecture).
*   **Môi trường**: Java 21, Spring Boot 4.0.2.

## 2. Công nghệ & Công cụ (Tech Stack)

### Backend (Spring Boot)
*   **Core**: Java 21, Spring Boot 4.0.2.
*   **Database**: PostgreSQL.
*   **Message Broker**: RabbitMQ (Async Emails).
*   **Storage**: MinIO (File/Avatar Storage).
*   **Auth**: OAuth2 Resource Server (JWT) + Redis (Refresh Token) + RabbitMQ (Async Notifications).
*   **API Docs**: Swagger/OpenAPI.
*   **Mapping**: MapStruct.

### Frontend (Vue.js)
*   **Framework**: Vue 3 (Composition API).
*   **State Management**: Pinia.
*   **Build Tool**: Vite.
*   **Router**: Vue Router.
*   **HTTP Client**: Axios.
*   **Quality**: ESLint, Prettier.
*   **DevTools**: Vite Plugin Vue Devtools.
*   **Language**: TypeScript.

## 3. Cấu trúc Dự án & Clean Architecture

### Nguyên tắc Thiết kế
*   **Dependency Rule**: Domain Layer không phụ thuộc vào bất kỳ layer nào khác.
*   **Database Agnostic**: Core Logic không phụ thuộc vào SQL/NoSQL. Sử dụng Repository Interface để trừu tượng hóa.
*   **UI Agnostic**: Backend chỉ trả về JSON, không phụ thuộc Frontend (Vue/React).

### Cấu trúc Layer chi tiết
1.  **Domain Layer** (`domain`):
    *   **Entities**: POJO thuần túy (hạn chế JPA annotation nếu cần tách biệt triệt để).
    *   **Repository Interfaces**: Contract (e.g., `UserRepository`).
2.  **Application Layer** (`application`):
    *   **Services**: Implement Use Cases.
    *   **DTOs**: Input/Output cho API.
3.  **Infrastructure Layer** (`infrastructure`):
    *   **Persistence**: Implement Repository Interface (JPA/Hibernate).
    *   **External Services**: Email, Storage impl.
4.  **API Layer** (`api`):
    *   **Controllers**: REST endpoints.

### Cấu trúc Thư mục Monorepo

```
cms-root/
├── pom.xml (Root Build Config)
├── src/ (Spring Boot Backend)
│   ├── main/java/vn/com/nws/cms/...
│   │   ├── common/
│   │   └── modules/
│   │       ├── auth/
│   │       ├── iam/
│   │       ├── academic/
│   │       ├── storage/ (New)
│   │       └── notification/ (New)
│   └── main/resources/
│
└── frontend/ (Vue.js Project)
    ├── package.json
    ├── vite.config.ts
    ├── src/
    │   ├── api/ (Axios services)
    │   ├── assets/
    │   ├── components/
    │   ├── layouts/
    │   │   ├── AdminLayout.vue (Sidebar, Header for Admin)
    │   │   └── UserLayout.vue (TopNav for Students)
    │   ├── router/
    │   ├── stores/ (Pinia)
    │   └── views/
    └── public/
```

### Các Module Bổ sung (Để hoàn thiện Clean Arch)
1.  **Storage Module**: Xử lý file upload (Interface: `StorageService`). Implement: Local/S3.
2.  **Notification Module**: Xử lý gửi mail/thông báo (Interface: `NotificationSender`). Implement: JavaMail.
3.  **Audit Module**: Ghi log hệ thống độc lập.

### Quy trình Build & Run
1.  **Development**:
    *   Spring Boot: Run port `8081`.
    *   Vue.js: Run `npm run dev` port `5173`.
    *   **Proxy**: Vite config proxy `/api` -> `localhost:8080`.
2.  **Production**:
    *   Maven Plugin (`frontend-maven-plugin`) sẽ:
        1.  Chạy `npm install`.
        2.  Chạy `npm run build`.
        3.  Copy folder `dist` vào `src/main/resources/static`.
    *   Kết quả: 1 file JAR duy nhất chứa cả Backend và Frontend.

### Seed dữ liệu dev
*   `DataSeeder` chỉ được nạp khi chạy profile `dev` và `cms.seed.enabled=true` ([DataSeeder](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/common/seed/DataSeeder.java)).
*   Trong repo hiện tại, `application-dev.properties` đặt `cms.seed.enabled=${CMS_SEED_ENABLED:true}` để mặc định bật seed ở dev và có thể tắt bằng env `CMS_SEED_ENABLED=false` (có thể khai báo trong `.env` do app đang `spring.config.import=optional:file:.env[.properties]`).
*   Việc đổi `spring.jpa.hibernate.ddl-auto` (validate/update) chỉ ảnh hưởng đồng bộ schema, không quyết định việc seeder có chạy hay không.

## 4. Coding Conventions & Rules

### Null Safety
*   **Entity & DTO**: Bắt buộc sử dụng Annotation Validation (`@NotNull`, `@NotBlank`) hoặc `Optional`.
*   **Service**: Kiểm tra null đầu vào, trả về Exception cụ thể nếu dữ liệu thiếu.

### Frontend Standards
*   **Internationalization (i18n)**: Cấu trúc code hỗ trợ đa ngôn ngữ ngay từ đầu (`vue-i18n`).
*   **Component**: Chia nhỏ component, tái sử dụng (Button, Input, Table).

## 5. Lộ trình Thực hiện (Roadmap)

### Giai đoạn 1: Foundation (Backend) - Đã hoàn thành
- [x] Setup Spring Boot, Security (OAuth2), Database.
- [x] Implement Auth Module (Login, Register) với Refresh Token lưu Redis.
- [x] Tích hợp RabbitMQ để gửi email bất đồng bộ (Welcome, Reset Password).
- [x] Tích hợp MinIO để lưu trữ file/avatar.

### Giai đoạn 2: Frontend Setup (Vue.js)
- [x] Khởi tạo dự án Vue 3 + Vite + TS trong thư mục `frontend`.
- [x] Cấu hình Axios instance + interceptor attach JWT.
- [x] Setup Router + guards `requiresAuth`.
- [x] Implement Login/Register/Forgot/Reset kết nối API.
- [x] Chuẩn UI nền tảng với Vuetify theme + components dùng lại (Phase 1).
- [x] Làm snackbar/toast global (Pinia UI store + hiển thị trong App).
- [x] Hoàn thiện luồng refresh token (401 → refresh → retry).
- [x] Thêm route-level authorization `roles` (admin-only routes).
- [x] Lazy-load routes (tối ưu bundle) theo Phase 3.
- [x] Chuẩn hoá helper parse `ApiResponse<T>`/`PageResponse<T>` để giảm lặp.

### Giai đoạn 3: Core Features (Fullstack)
- [ ] **User Management**: API + UI (Profile, Admin Dashboard).
- [ ] **Academic**: API + UI (Quản lý Học kỳ, Môn học).
- [ ] **Academic - Lớp hành chính (StudentClass)**: Đồng nhất API `student-classes`, loại bỏ `admin-classes`, và đảm bảo FE/BE map đúng dữ liệu (form v-model, danh sách sinh viên).

### Giai đoạn 4: E-learning Features
N/A (ngoài phạm vi dự án hiện tại).

## 6. RBAC (Phân quyền) — Hiện trạng & kế hoạch chuyển đổi

- Hiện trạng phân quyền đang dựa nhiều vào `@PreAuthorize(hasRole...)` (hardcode policy trong code) trong khi database đã có đầy đủ bảng RBAC (`roles`, `permissions`, `role_permissions`, `user_roles`) nhưng chưa được tận dụng để enforce theo permission.
- Đã triển khai bước đầu: đưa permission (từ DB) vào JWT `scope` khi login/refresh để sẵn sàng chuyển dần sang `hasAuthority('X:Y')`.
- Tài liệu tổng hợp chức năng theo role + lộ trình chuyển sang phân quyền động theo permission: [RBAC_CURRENT_FEATURES_AND_PLAN.md](file:///c:/Users/Admin/Downloads/Repo/cms/docs/RBAC_CURRENT_FEATURES_AND_PLAN.md).
