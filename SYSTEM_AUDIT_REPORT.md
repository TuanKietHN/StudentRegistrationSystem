# Báo Cáo Kiểm Tra Hệ Thống và Business Logic

## 1. Tổng Quan
Hệ thống CMS hiện tại được xây dựng dựa trên kiến trúc Modular Monolith với Spring Boot (Backend) và Vue.js 3 (Frontend). Cấu trúc dự án rõ ràng, phân chia theo các module nghiệp vụ (`academic`, `auth`, `iam`).

## 2. Đánh Giá Kiến Trúc (Strict Clean Architecture)

### 2.1. Điểm Tuân Thủ Tốt
- **Phân tầng rõ ràng**: Có sự tách biệt giữa `api` (Controller), `application` (Service), `domain` (Model/Repository Interface), và `infrastructure` (Persistence Implementation).
- **Service Layer**: Logic nghiệp vụ được đặt trong `ServiceImpl`, không bị rò rỉ ra Controller.
- **Repository Pattern**: Sử dụng Interface tại Domain layer và Implementation tại Infrastructure layer (`CohortRepository` vs `CohortRepositoryImpl`).
- **Mapping**: Sử dụng **MapStruct** để chuyển đổi giữa DTO, Domain Model và Entity, giúp tách biệt các lớp dữ liệu.
- **DTO**: Controller sử dụng DTO cho Request/Response, không trả về trực tiếp Entity hay Domain Model.

### 2.2. Vi Phạm Nghiêm Trọng (Cần Khắc Phục Ngay)
- **Domain Model không phải Pure POJO**:
  - Các Domain Model (ví dụ: `Cohort.java`, `User.java`) đang kế thừa từ `AuditEntity`.
  - `AuditEntity` chứa các annotation của JPA/Spring Data như `@MappedSuperclass`, `@EntityListeners`, `@Column`.
  - **Hậu quả**: Domain Layer bị phụ thuộc vào Framework (JPA), vi phạm nguyên tắc cốt lõi của Clean Architecture là Domain phải độc lập với Infrastructure.
  - **Khuyến nghị**: Tạo một lớp `Audit` POJO riêng cho Domain hoặc nhúng các trường audit trực tiếp vào Domain Model mà không có annotation. `AuditEntity` của JPA chỉ nên dùng cho các Entity trong `infrastructure`.

## 3. Đánh Giá Business Logic

### 3.1. Module Auth (Xác thực & Phân quyền)
- **Cơ chế JWT**: Triển khai tốt, sử dụng Access Token (Stateless) và Refresh Token (Opaque).
- **Refresh Token Rotation**:
  - Logic **rất chặt chẽ**. Hệ thống sử dụng Redis để theo dõi chuỗi token.
  - Tính năng **Reuse Detection** hoạt động đúng: Nếu phát hiện token cũ được sử dụng lại, hệ thống sẽ khóa tài khoản người dùng và thu hồi toàn bộ phiên đăng nhập.
  - Sử dụng `watch/multi/exec` của Redis để đảm bảo tính nguyên tử (Atomicity).
- **Hạn chế**: Frontend chưa gửi thông tin `activeRole` (vai trò đang kích hoạt) trong header của request. Điều này sẽ gây khó khăn cho Backend khi xử lý người dùng có nhiều vai trò (ví dụ: vừa là Giáo viên vừa là Sinh viên).

### 3.2. Module Academic (Quản lý đào tạo)
- **Repository Implementation**: Tuân thủ quy tắc sử dụng HQL/JPQL trong `@Query` thay vì Native SQL, giúp hệ thống độc lập hơn với loại database cụ thể.
- **Service Logic**: Các luồng tạo mới, cập nhật Cohort được xử lý giao dịch (`@Transactional`) và validate đầy đủ.

## 4. Đánh Giá Frontend (Vue.js)

### 4.1. Điểm Tốt
- Sử dụng **Pinia** để quản lý state (Auth Store).
- Sử dụng **Composition API** (`<script setup>`) đồng bộ.
- Có cơ chế **Interceptor** để tự động đính kèm Token và xử lý Refresh Token khi gặp lỗi 401.

### 4.2. Vấn đề cần khắc phục
- **Thiếu Context Role trong API Request**:
  - Theo quy tắc dự án: "When an API call requires role context, attach `activeRole` via a request header".
  - Hiện tại: File `frontend/src/api/axios.ts` chỉ đính kèm `Authorization` header mà chưa đính kèm header `X-Active-Role` (hoặc tương tự) từ `localStorage` hay Pinia Store.

## 5. Kết Luận và Kiến Nghị

### Các hành động ưu tiên cao:
1.  **Refactor Domain Models**: Loại bỏ sự phụ thuộc của Domain Model vào `AuditEntity` (JPA). Tách `AuditEntity` thành 2 phiên bản: một cho Domain (POJO) và một cho Infrastructure (JPA).
2.  **Cập nhật Axios Interceptor**: Bổ sung logic lấy `activeRole` từ `localStorage` và gửi kèm trong header của mọi request API để Backend xác định ngữ cảnh xử lý.

### Các hành động duy trì:
- Tiếp tục duy trì việc sử dụng HQL trong Repository.
- Đảm bảo mọi API mới đều có Swagger documentation tiếng Việt.
