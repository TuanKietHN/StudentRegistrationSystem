# Seeding Strategy (Flyway-first)

## Nguyên tắc

- Flyway là nguồn sự thật duy nhất cho schema.
- Seed data cần phân loại rõ:
  - **Baseline/reference data**: dữ liệu nền tảng, deterministic, dùng được cho mọi môi trường.
  - **Demo/dev data**: dữ liệu phục vụ dev, không bắt buộc ở production.
 - Nếu DB bị “drift” (schema không còn khớp migrations), ưu tiên reset DB để quay về trạng thái khớp code.

## Khuyến nghị áp dụng trong dự án này

### 1) Baseline/reference data: dùng Flyway

Nên seed bằng Flyway migrations, đảm bảo idempotent (ví dụ dùng `ON CONFLICT DO NOTHING`).

Ví dụ đang có:
- V1 seed một số dữ liệu nền tảng (`roles`, `permissions`) và dữ liệu mẫu (semesters/subjects/users/courses).

### 2) Demo/dev data: dùng Application Seeder (dev profile)

Dùng seeder trong code chỉ khi:
- Có guard theo profile/property để tránh chạy ở production.
- Có điều kiện tồn tại để không tạo trùng.

Dự án hiện dùng:
- `DataSeeder` chỉ chạy khi:
  - profile `dev`
  - `cms.seed.enabled=true`

Cấu hình:
- `application.properties`: `cms.seed.enabled=false`
- `application-dev.properties`: `cms.seed.enabled=${CMS_SEED_ENABLED:true}` (mặc định bật, có thể tắt bằng env `CMS_SEED_ENABLED=false`)

### 4) Quy ước “code-first” cho database trong dự án này

- “Tạo database từ code” nghĩa là tạo schema bằng Flyway migrations trong repo.
- Nếu gặp lỗi kiểu “entity không có field nhưng DB lại bắt NOT NULL”, đó là dấu hiệu DB drift.
- Cách xử lý chuẩn: reset database/schema rồi chạy app để Flyway migrate lại từ đầu:
  - [DEV_DB_RESET.md](file:///c:/Users/Admin/Downloads/Repo/cms/docs/DEV_DB_RESET.md)

### 3) Khi nào dùng Repeatable migration (`R__*.sql`)?

Chỉ nên dùng cho dữ liệu reference “có thể update” và vẫn idempotent.
Nếu dùng repeatable cho demo data, cần quy ước tách theo môi trường để tránh seed demo vào production.
