# Security Review (Auth/Session/RBAC/SQLi) — Dự án CMS

Tài liệu này tổng hợp hiện trạng bảo mật của dự án (backend Spring Boot + frontend Vue), trả lời các câu hỏi về phân quyền, session đa thiết bị, SQL injection, rủi ro token bị copy, và đề xuất giải pháp theo hướng “chuẩn doanh nghiệp”.

## 1) Phân quyền tĩnh hay động?

### Kết luận

- Dự án đang dùng RBAC theo role (vai trò) và phần lớn là “tĩnh theo code”.
- Role được lưu trong DB, nhưng tập role hợp lệ bị “đóng” theo enum nên không phải mô hình phân quyền động theo permission.

### Bằng chứng trong code

- Role enum cố định: [RoleType.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/domain/enums/RoleType.java#L1-L11) chỉ có `ADMIN/TEACHER/STUDENT`.
- Backend check role bằng annotation (hard-code): ví dụ [UserController.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/iam/api/UserController.java) và các controller academic dùng `@PreAuthorize("hasRole('ADMIN')")`.
- DB có bảng `permissions` và `role_permissions` (định hướng permission-based), nhưng hiện chưa thấy code load permission → authority để enforce: [V2__add_authentication_tables_v2.sql](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/resources/db/migration/V2__add_authentication_tables_v2.sql#L12-L59).

### Rủi ro đang tồn tại

- “Dynamic permission” trong DB chưa được dùng nên việc gán quyền chi tiết theo nghiệp vụ (course:create, user:read, …) chưa thực thi được.
- Có nguy cơ lệch mapping authority khi chạy bằng JWT Resource Server: access token đang nhét role vào claim `scope` dạng `ROLE_ADMIN ...` ([JwtProvider.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/common/security/JwtProvider.java#L41-L56)), nhưng resource server đang dùng mặc định (`jwt(withDefaults())`) ([SecurityConfig.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/common/config/SecurityConfig.java#L29-L44)), thường map scope thành `SCOPE_...`. Điều này có thể làm `hasRole('ADMIN')` không khớp trong runtime.

### Đề xuất “chuẩn doanh nghiệp”

- Nếu chỉ cần RBAC cơ bản: chuẩn hoá claim roles (ví dụ `roles: ['ADMIN']`) và cấu hình converter để map đúng về `ROLE_...`.
- Nếu cần phân quyền động: triển khai permission-based:
  - Load permissions từ DB theo user/role.
  - Map thành `GrantedAuthority` dạng `PERM_COURSE_CREATE` hoặc `COURSE:CREATE`.
  - Dùng `hasAuthority(...)`/custom permission evaluator thay vì chỉ `hasRole(...)`.

## 2) Ứng dụng quản lý session như nào?

### Kết luận

- Backend chạy stateless theo JWT (không dùng HTTP session).
- “Session” thực tế được mô phỏng bằng refresh token lưu Redis.

### Bằng chứng

- Stateless: [SecurityConfig.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/common/config/SecurityConfig.java#L31-L40) đặt `SessionCreationPolicy.STATELESS`.
- Access token là JWT HS256: [JwtConfig.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/common/config/JwtConfig.java#L18-L42).
- Refresh token lưu Redis và có rotate/reuse detection: [AuthenticationService.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/auth/application/AuthenticationService.java#L43-L132).

### Lưu ý cấu hình TTL

- `jwt.expiration=3600000` đang là milliseconds (1 giờ) và được dùng đúng (`plusMillis`).
- `jwt.refresh-expiration=86400` đang được dùng như **milliseconds** (`Duration.ofMillis(refreshExpiration)`) ⇒ TTL chỉ ~86.4 giây, rất có khả năng sai ý định (thường mong muốn 1–30 ngày).
  - Tham chiếu: [application.properties](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/resources/application.properties#L13-L16) và [AuthenticationService.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/auth/application/AuthenticationService.java#L37-L41).

## 3) Nhiều thiết bị cùng đăng nhập: timeout/ghi đè session/xử lý session cũ

### Hiện trạng dự án (đang làm gì?)

- Dự án đang “enforce single session” ở tầng refresh token:
  - Redis key `auth:u:rt:{username}` chỉ cho phép 1 refresh token active.
  - Đăng nhập trên thiết bị mới sẽ xoá refresh token cũ: [AuthenticationService.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/auth/application/AuthenticationService.java#L122-L132).
- Khi refresh token cũ bị ghi đè:
  - Thiết bị cũ sẽ không refresh được (bị 401 khi access token hết hạn).
  - Nhưng access token cũ vẫn có thể “active” cho đến khi tự hết hạn (vì JWT là stateless).

### “Session cũ vẫn còn” thì xử lý sao cho đúng?

Trong mô hình JWT stateless, “session cũ” về bản chất là access token đã phát ra. Để “tắt ngay” token cũ, cần thêm state server-side. Các hướng thực tế:

- Hướng A (phổ biến nhất): access token rất ngắn + refresh rotation
  - Access token 5–15 phút, refresh 7–30 ngày.
  - Khi “đá thiết bị”, chỉ cần revoke refresh token; thiết bị cũ sẽ bị out trong vòng 5–15 phút.
- Hướng B: session id (sid) + Redis allowlist
  - Mỗi login tạo `sid` (hoặc `jti`) và lưu Redis “active sessions”.
  - JWT mang claim `sid`; mỗi request kiểm tra `sid` còn active không.
  - Cho phép revoke ngay lập tức, đổi password/lock account → revoke all.
- Hướng C: token blacklist theo `jti`
  - Khi logout/lock account, add `jti` vào blacklist Redis tới khi token hết hạn.
  - Overhead tăng vì cần check Redis cho mọi request.

### “Nên dùng time-out” trong trường hợp đa thiết bị?

Khuyến nghị triển khai cả 2 loại timeout (chuẩn doanh nghiệp):

- Absolute timeout: refresh token hết hạn sau N ngày kể từ khi phát.
- Inactivity timeout: nếu không hoạt động N ngày thì session đó expire (cập nhật `lastUsedAt`/sliding TTL).

### Dự án hiện tại đã xử lý được bài toán này chưa?

- Có một phần: refresh token rotation + reuse detection + single active refresh token: [AuthenticationService.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/auth/application/AuthenticationService.java#L65-L110).
- Chưa đầy đủ cho “đa thiết bị” theo chuẩn enterprise:
  - Không có khái niệm “session theo thiết bị” (device id/user-agent/ip/lastSeen).
  - Không có danh sách session để user/admin xem và revoke theo thiết bị.
  - Không có cơ chế revoke ngay access token cũ (chỉ chờ hết hạn).

## 4) SQL Injection: kiểm tra như nào là chuẩn doanh nghiệp thực tế nhất?

### Hiện trạng code (bề mặt SQLi)

- Không thấy dùng native SQL/JdbcTemplate/EntityManager query động; phần lớn là JPQL `@Query` với named parameters `:param` ⇒ an toàn trước SQL injection ở mức ORM.
- Ví dụ JPQL có bind param: [JpaCourseRepository.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/academic/infrastructure/persistence/repository/JpaCourseRepository.java), [JpaUserRepository.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/auth/infrastructure/persistence/repository/JpaUserRepository.java).

### “Chuẩn doanh nghiệp” (process + controls)

- Code-level controls
  - Tuyệt đối không ghép chuỗi tạo query từ input (nhất là ORDER BY/field name).
  - Khi cần query động: dùng Criteria/Specification và whitelist sort fields.
  - Hạn chế `LIKE %keyword%` với keyword quá dài; cân nhắc escape `%/_` và giới hạn độ dài để tránh query nặng.
- SDLC controls
  - SAST (Semgrep/SonarQube) rule-set cho SQLi/ORM injection.
  - Dependency scanning (SCA) để phát hiện CVE thư viện.
  - DAST (OWASP ZAP/Burp) chạy trên môi trường staging với test cases.
  - Security code review checklist bắt buộc cho các thay đổi liên quan query/filter/sort.
- Runtime controls
  - Observability: log slow queries, rate-limit theo IP/user cho endpoint search.
  - WAF/Ingress rule: chặn pattern tấn công phổ biến (bổ trợ, không thay thế fix code).

## 5) Token đọc được trong F12, copy sang Postman vẫn dùng được: xử lý như nào?

### Bản chất vấn đề

- Access token là Bearer token: ai cầm token thì dùng được cho tới khi hết hạn.
- Trên SPA, nếu token được lưu ở `localStorage` thì token có thể bị đọc khi có XSS hoặc bị copy từ devtools.
- Hiện trạng frontend đang lưu cả access/refresh token trong `localStorage`: [auth.ts](file:///c:/Users/Admin/Downloads/Repo/cms/frontend/src/stores/auth.ts#L17-L40), và axios đọc từ `localStorage` để gắn header: [axios.ts](file:///c:/Users/Admin/Downloads/Repo/cms/frontend/src/api/axios.ts#L10-L19).

### Giải pháp thực tế (không có “chặn tuyệt đối”)

- Giảm khả năng token bị đánh cắp
  - Ưu tiên chuyển refresh token sang HttpOnly Secure SameSite cookie (không đọc được từ JS).
  - Áp CSP chặt (không inline script), loại bỏ chỗ có nguy cơ XSS (đặc biệt `v-html`/render HTML thô).
- Giảm tác hại nếu token bị lộ
  - Access token ngắn hạn (5–15 phút) + rotation refresh token.
  - Phát hiện bất thường (IP/UA thay đổi mạnh, refresh reuse, velocity) và revoke session.
- “Proof-of-possession” (nâng cao)
  - DPoP/MTLS/token binding chỉ phù hợp hơn cho native/mobile; web browser có hạn chế. Thường thay bằng BFF pattern.

## 6) Block tài khoản khi nghi ngờ bị trộm key/token

### Hiện trạng

- Chưa thấy cơ chế account lock/rate-limit/brute-force protection.
- `UserDetails` không set disabled/locked flags: [CustomUserDetailsService.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/auth/infrastructure/CustomUserDetailsService.java#L20-L33).
- Có refresh token reuse detection (một dạng signal “token bị đánh cắp”): [AuthenticationService.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/modules/auth/application/AuthenticationService.java#L74-L84).

### Đề xuất

- Thêm trạng thái user: `locked/disabled`, `failedLoginCount`, `lockedUntil`, `lastLoginAt`, `lastLoginIp`.
- Brute-force protection:
  - Rate limit theo IP + theo username cho `/auth/login` và `/auth/refresh`.
  - Khoá tạm thời sau N lần fail; gửi email cảnh báo.
- Khi nghi ngờ token bị lộ:
  - Revoke tất cả refresh tokens của user (và bump “security stamp/session version” để invalidate access token).
  - Ép đổi mật khẩu + bật 2FA (nếu có).

## 7) Các vấn đề bảo mật/thiết kế đang thấy thêm trong dự án

- Cần đảm bảo secrets/credentials không commit vào repo; hiện đã chuyển cấu hình sang env vars và hỗ trợ `.env` local: [application.properties](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/resources/application.properties) và [.env.example](file:///c:/Users/Admin/Downloads/Repo/cms/.env.example).
- JWT ký HS256 với shared secret (tăng rủi ro nếu lộ secret, khó rotation chuẩn enterprise): [JwtConfig.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/common/config/JwtConfig.java#L18-L42).
- `spring.jpa.show-sql`/`format_sql` cần tắt mặc định và chỉ bật theo profile dev: [application.properties](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/resources/application.properties) và [application-dev.properties](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/resources/application-dev.properties).
- CORS origins hard-code cho localhost và `allowCredentials=true` trong config chung: [WebConfig.java](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/java/vn/com/nws/cms/common/config/WebConfig.java#L15-L25). Nên tách theo profile và cấu hình hoá.

## 8) Ưu tiên triển khai (đề xuất)

### Mức P0 (nguy cơ cao / sửa sớm)

- Đã chuyển secrets khỏi repo (env vars + `.env` local).
- Đã chuẩn hoá `jwt.refresh-expiration` theo milliseconds (giá trị mặc định 7 ngày).
- Đã chuẩn hoá authority mapping JWT ↔ Spring để `@PreAuthorize` enforce đúng.

### Mức P1 (nâng chuẩn enterprise)

- Đã chuyển refresh token sang HttpOnly cookie; frontend không còn lưu refresh token trong localStorage.
- Đã thêm session theo thiết bị (list/revoke/revoke-all) và giới hạn số phiên tối đa theo tài khoản.
- Đã thêm rate limit login, account lock theo số lần sai, và audit log vào DB.

### Mức P2 (mở rộng phân quyền)

- Triển khai permission-based authorization theo bảng `permissions/role_permissions` đã có trong schema.
