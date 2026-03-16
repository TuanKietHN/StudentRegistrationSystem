# Plan refactor UI (chọn framework tốt nhất cho CMS)

## 1) Kết quả phân tích & quyết định

Sau khi so sánh Vuetify / PrimeVue / Quasar / Tailwind (xem [UI_FRAMEWORK_ANALYSIS.md](file:///c:/Users/Admin/Downloads/Repo/cms/docs/UI_FRAMEWORK_ANALYSIS.md)), lựa chọn framework UI tốt nhất cho dự án CMS hiện tại là:

**Vuetify 3 (giữ nguyên) + refactor theo hướng Design System và Enterprise CRUD patterns.**

Mục tiêu: “đẹp” hơn, nhất quán hơn, dễ mở rộng hơn, nhưng không tăng rủi ro bằng việc replatform sang UI framework khác.

## 2) Nguyên tắc refactor (enterprise-grade)

### 2.1. Design System trước, màn hình sau
- Tạo theme tokens: màu, typography, density, border radius, elevation, spacing scale.
- Chuẩn hoá patterns:
  - Page layout (header, actions, filters, table, empty/loading)
  - Dialog CRUD (create/update)
  - Confirm delete dialog
  - Notification/snackbar

### 2.2. Contract với backend Spring Boot
- Giữ cấu trúc API hiện có:
  - `ApiResponse<T>`, `PageResponse<T>`
- Chuẩn hoá parse/handle lỗi:
  - `401` → logout + redirect login (đã có)
  - `403` → snackbar “Không có quyền”
  - validation errors map field → message → hiển thị trong form
- Role-based UI: không thay thế security backend, chỉ hỗ trợ UX.

### 2.3. Tối ưu maintainability
- Giảm lặp code giữa các màn CRUD (đặc biệt search debounce + pagination + dialog).
- Tách reusable components/composables thay vì copy-paste.

## 3) Kiến trúc refactor đề xuất (Vue 3 + Vuetify)

### 3.1. Cấu trúc mới (mở rộng từ hiện tại)
- `src/plugins/vuetify.ts`: theme + defaults + icons
- `src/components/ui/`
  - `PageHeader.vue`
  - `ConfirmDialog.vue`
  - `CrudDialog.vue` (tuỳ mức độ)
- `src/composables/`
  - `useDebouncedRef.ts` hoặc `useDebounceFn.ts`
  - `useServerPagination.ts` (optional)
- `src/api/`
  - chuẩn hoá axios instance & error mapping (giữ tương thích)

### 3.2. Chuẩn UI cho CRUD pages
- Header:
  - Title (text-h6/h5)
  - Action buttons (primary)
- Filters:
  - đặt trong `v-row`, responsive
  - debounce search input
- Data table:
  - ưu tiên `v-data-table-server` nếu cần chuẩn hoá sorting/paging
  - hoặc giữ `v-table` nhưng chuẩn hoá toolbar/empty state
- Dialog:
  - create/update dùng 1 dialog
  - delete confirmation dùng confirm dialog
- Feedback:
  - snackbar cho success/error
  - loading indicator rõ ràng

## 4) Lộ trình triển khai (phased rollout)

### Phase 1 (ngay)
1) Thiết lập Vuetify theme + defaults để UI “đẹp” và nhất quán hơn.
2) Tạo các UI components dùng lại:
   - `PageHeader`, `ConfirmDialog`.
3) Refactor các màn list hiện có để dùng components chung (giảm lặp):
   - Departments, Teachers, Semesters, Subjects, Courses, Users.
4) Xác minh build/typecheck.

### Phase 2
1) Chuẩn hoá DataTable server-side:
   - sorting, items-per-page, sticky header, column config.
2) Chuẩn hoá form validation:
   - mapping lỗi backend → field-level errors.
3) Cache danh mục dùng chung (semesters/subjects/departments) bằng Pinia store.

### Phase 3
1) Lazy-load routes (tối ưu bundle).
2) Skeleton/loading UX.
3) Thiết lập CI quality gate: typecheck + lint + build.

## 5) Tiêu chí “đẹp” (định nghĩa rõ)

- Nhất quán spacing/density, alignment chuẩn.
- Typography rõ ràng (heading/subtitle/body).
- Button hierarchy (primary/secondary/text).
- Table có header rõ, empty/loading state lịch sự.
- Dialog có flow đúng: validate, loading, close on success.
- Responsive: desktop/tablet/mobile không vỡ layout.

## 6) Kế hoạch refactor trong repo (đã bắt đầu)

Sau khi tạo file plan này:
- Bắt đầu refactor theo **Phase 1**: theme + components chung + chỉnh các màn list để nhất quán.

