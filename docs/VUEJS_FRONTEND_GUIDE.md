# Vue.js Frontend Guide (CMS)

## 1) Vue.js hoạt động như thế nào (tổng quan)

### 1.1. SPA (Single Page Application) là gì?
- SPA tải **một trang HTML ban đầu** (thường là `index.html`), sau đó dùng JavaScript để:
  - Render giao diện theo **route** (URL).
  - Gọi API lấy dữ liệu và cập nhật UI mà **không reload toàn trang**.
  - Chuyển trang “mượt” bằng router phía client (history API).

### 1.2. Vue render UI như thế nào?
- Vue tạo cây component và render dựa trên **state** (dữ liệu reactive).
- Khi state thay đổi, Vue tự tính lại những phần UI bị ảnh hưởng (reactivity + virtual DOM).
- Với Vue 3, reactivity dựa trên `Proxy` và ưu tiên cập nhật theo cơ chế batch/microtask.

### 1.3. Component trong Vue
- Component = một đơn vị UI có:
  - `template` (giao diện)
  - `script` (logic)
  - `style` (CSS)
- Component có thể nhận `props`, phát `events`, và sử dụng state nội bộ.
- Vue 3 khuyến nghị dùng **Composition API** (đặc biệt với TypeScript).

### 1.4. Lifecycle (vòng đời)
- `onMounted`: chạy sau khi component được mount lên DOM (thường dùng fetch dữ liệu).
- `onUnmounted`: cleanup (cancel request, remove listener).
- `watch`/`watchEffect`: phản ứng theo thay đổi state/props.

### 1.5. Router (điều hướng)
- Router chịu trách nhiệm map URL → component.
- Guard cho phép chặn/redirect dựa trên trạng thái auth/role.
- SPA dùng `createWebHistory` để URL “đẹp” (không có `#`).

### 1.6. State management (Pinia)
- Store chứa state dùng chung giữa nhiều màn hình: auth, ui, cache danh mục,…
- Best practice: store giữ state “business/global”, component giữ state UI cục bộ.

---

## 2) Dự án này có đang dùng Vue SPA không?

Có. Frontend hiện tại là **Vue 3 SPA** chạy bằng Vite.

- Entry point: [frontend/src/main.ts](file:///c:/Users/Admin/Downloads/Repo/cms/frontend/src/main.ts)
  - `createApp(App).use(pinia).use(router).use(vuetify).mount('#app')`
- Router client-side: [frontend/src/router/index.ts](file:///c:/Users/Admin/Downloads/Repo/cms/frontend/src/router/index.ts)
- Build tool: Vite (scripts `dev/build/preview`) trong [frontend/package.json](file:///c:/Users/Admin/Downloads/Repo/cms/frontend/package.json)
- `index.html` của Vite chỉ là shell ban đầu, còn nội dung chính do Vue render.

---

## 3) Kiến trúc frontend hiện tại trong repo

### 3.1. Công nghệ
- Vue 3 + TypeScript
- Vue Router
- Pinia (state)
- Vuetify 3 (UI framework)
- Axios (HTTP client)

### 3.2. Cấu trúc thư mục (thực tế)
- `frontend/src/views`: các màn hình (route pages)
- `frontend/src/api`:
  - `axios.ts`: axios instance + interceptor
  - `services/*`: gọi API theo từng module (auth, departments, teachers, semesters, subjects, courses, users)
- `frontend/src/stores`:
  - `auth.ts`: trạng thái đăng nhập
  - `ui.ts`: snackbar/notifications
- `frontend/src/router`: cấu hình routes/guards

### 3.3. Quy ước gọi API hiện tại
- Axios baseURL: `/api` trong [frontend/src/api/axios.ts](file:///c:/Users/Admin/Downloads/Repo/cms/frontend/src/api/axios.ts)
- Dev proxy (Vite): `/api` → `http://localhost:8081` trong [frontend/vite.config.ts](file:///c:/Users/Admin/Downloads/Repo/cms/frontend/vite.config.ts)
- Interceptor request tự gắn `Authorization: Bearer <token>` từ `localStorage`.
- Interceptor response xử lý `401` bằng cách xoá token và redirect `/login`.

---

## 4) Plan implements theo backend Spring Boot (frontend ↔ backend)

Mục tiêu: frontend triển khai tính năng theo từng module backend một cách ổn định, dễ mở rộng, giảm rủi ro khi backend thay đổi.

### 4.1. Chuẩn hoá contract API
- Backend trả về wrapper `ApiResponse<T>` và phân trang `PageResponse<T>`.
  - Yêu cầu frontend parse thống nhất:
    - `response.data.data` cho payload
    - `response.data.message` cho thông báo
- Quy ước lỗi:
  - Validation: backend có thể trả `data` dạng map field → message.
  - Frontend hiển thị message + chi tiết field (đã áp dụng ở các màn hình).

### 4.2. Tổ chức services theo module backend
- Mỗi module backend tương ứng 1 service file:
  - `department.service.ts`, `teacher.service.ts`, `semester.service.ts`, `subject.service.ts`, `course.service.ts`, `user.service.ts`
- Service chỉ làm nhiệm vụ:
  - map endpoint + query params
  - không xử lý UI/route trong service

### 4.3. Router & phân quyền
- Backend có `@PreAuthorize("hasRole('ADMIN')")` cho create/update/delete nhiều module.
- Plan frontend:
  - Guard: chặn route admin-only (nếu muốn cứng), hoặc soft-guard (disable nút).
  - UI: ẩn/disable nút thao tác khi user không có role phù hợp.
  - Đồng thời vẫn phải xử lý trường hợp backend trả `403` (snackbar).

### 4.4. Luồng authentication với Spring Security (JWT)
- Đăng nhập thành công → lưu `accessToken` vào storage → interceptor tự gắn token cho các request.
- Khi token hết hạn / backend trả `401`:
  - Xoá token + redirect login.
- Kế hoạch chuẩn hơn cho doanh nghiệp (khuyến nghị):
  - Dùng refresh token đúng nghĩa hoặc chuyển sang cookie HttpOnly + CSRF strategy.
  - Tránh lưu refresh token ở localStorage nếu hệ thống yêu cầu bảo mật cao.

### 4.5. CORS/Dev setup
- Dev: Vite proxy `/api` để tránh CORS, không cần cấu hình CORS phức tạp.
- Nếu chạy FE/BE khác domain (staging/prod) thì cần:
  - cấu hình CORS ở Spring Boot (đã có WebConfig cho localhost).
  - đảm bảo `allowCredentials` và `allowedOrigins` phù hợp.

### 4.6. Plan triển khai tính năng (từng bước)
1) Backend hoàn thiện endpoint + DTO + validation + security.
2) Frontend tạo `api/services/<module>.service.ts` tương ứng.
3) Frontend tạo `views/<module>/<List>.vue` theo pattern:
   - search/filter/pagination
   - dialog create/update
   - dialog confirm delete
   - snackbar notify
4) Router thêm route + menu sidebar.
5) Kiểm thử thủ công theo checklist (role admin/non-admin, error flows).
6) Build FE + verify không lỗi typecheck.

---

## 5) Yêu cầu chuẩn cho dự án doanh nghiệp (Vue + Spring Boot)

### 5.1. Yêu cầu phi chức năng (NFR)
- Hiệu năng:
  - TTFB/TTI tốt, hạn chế bundle phình.
  - Caching danh mục (semesters/subjects) hợp lý.
- Bảo mật:
  - Không log token/PII ở client.
  - XSS prevention (không render HTML không tin cậy).
  - Storage token theo mức độ yêu cầu bảo mật (ưu tiên HttpOnly cookie).
- Độ tin cậy:
  - Error handling thống nhất (snackbar + empty states).
  - Retry/backoff có kiểm soát cho một số API quan trọng.
- Quan sát/giám sát:
  - Error tracking (Sentry hoặc tương đương).
  - Log tối thiểu và có thể bật/tắt theo env.
- Khả năng mở rộng:
  - Module hoá theo domain backend.
  - Component/Composables tái sử dụng.

### 5.2. Yêu cầu về coding standard
- TypeScript strict ở mức phù hợp.
- ESLint + Prettier chạy trong CI.
- Quy ước naming:
  - `views/*` cho pages
  - `components/*` cho reusable components
  - `api/services/*` cho API clients
  - `stores/*` cho state global

### 5.3. Testing & Quality Gate
- Unit test:
  - composables/helpers/services (mock axios).
- Component test:
  - form validation, dialog flows.
- E2E (khuyến nghị):
  - login, CRUD (admin), read-only (non-admin).
- CI:
  - typecheck + lint + test + build.

---

## 6) Best practices với Vue (áp dụng cho dự án này)

### 6.1. Component design
- Ưu tiên Composition API + `<script setup lang="ts">`.
- Tránh component quá lớn:
  - tách dialog form ra component con khi logic phức tạp.
- Không trộn logic API vào template; để trong functions rõ ràng.

### 6.2. Quản lý state
- Pinia store cho:
  - auth session + role
  - ui notifications
  - cache danh mục dùng chung (semesters, subjects, departments)
- Dùng `computed` để derive state, tránh duplicate state.

### 6.3. Xử lý form & validation
- Dùng rule functions cho Vuetify input.
- Backend validation là nguồn sự thật cuối:
  - mapping lỗi field → hiển thị rõ.

### 6.4. HTTP/API
- Một axios instance duy nhất, có interceptor auth + 401 handling.
- Không “nuốt” lỗi:
  - throw lại lỗi, UI layer quyết định thông báo.
- Chuẩn hoá parse response:
  - tránh viết `response.data.data` lặp lại bằng helper/composable (khuyến nghị mở rộng).

### 6.5. Routing & Access control
- Guard cần kiểm tra:
  - `requiresAuth`
  - `requiresRole` (nếu triển khai)
- UI disable/ẩn nút không thay thế cho security backend.

### 6.6. Performance
- Lazy-load routes (khuyến nghị): `() => import('...')` cho views lớn.
- Cache lookup data:
  - `semesterOptions/subjectOptions` dùng nhiều màn hình → đưa vào store cache.
  - 
### 6.7. Notification
- Dùng Vuetify snackbar toast notification cho thông báo success/error.
- Làm toast global
- Hiển thị error message rõ ràng từ backend.
- Có thể tắt/ẩn snackbar khi cần.
---

## 7) Checklist triển khai module mới (chuẩn doanh nghiệp)

- API backend có:
  - DTO rõ ràng + validation
  - status code đúng (`401/403/404/409/422`)
  - lỗi trả về theo cấu trúc thống nhất
- Frontend có:
  - service file + types tương ứng
  - màn hình list có search/filter/pagination
  - create/update/delete có dialog + confirm
  - snackbar cho success/error
  - empty/loading states
  - quyền admin-only được reflect trong UI
- Quality:
  - typecheck pass
  - build pass
  - không hardcode URL backend (dùng proxy/env)

