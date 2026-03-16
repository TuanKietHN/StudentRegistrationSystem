# Reset Database theo code (Flyway)

Mục tiêu: đảm bảo schema trong database khớp 100% với code hiện tại (Flyway migrations trong repo) và seeder chạy ổn định.

## Khi nào cần reset?

- Gặp lỗi kiểu “column X violates not-null constraint” nhưng trong entity không có field tương ứng (DB drift).
- Bạn đã chạy migration ở trạng thái không đồng nhất, hoặc có chỉnh schema thủ công.
- Muốn test UI với dữ liệu demo/dày dữ liệu theo seeder mà không vướng dữ liệu cũ.

## Cách 1 (khuyến nghị): Drop & tạo lại database

Ví dụ database đang là `cm2`:

```sql
-- chạy bằng user có quyền
DROP DATABASE IF EXISTS cm2;
CREATE DATABASE cm2;
```

Sau đó chạy backend với profile `dev`:
- Flyway sẽ tự migrate từ đầu theo `src/main/resources/db/migration`
- Seeder sẽ chạy nếu `cms.seed.enabled=true` (đã bật ở `application-dev.properties`)

## Cách 2: Drop schema public (nếu không muốn drop database)

```sql
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
```

Sau đó chạy backend lại để Flyway migrate.

## Lưu ý

- Trong dự án này, **Flyway là nguồn sự thật cho schema** (code-first theo nghĩa “schema đến từ code trong repo”).
- `spring.jpa.hibernate.ddl-auto=validate` nghĩa là JPA không tự tạo bảng; nó chỉ validate schema đã có.
- Nếu muốn “tạo schema từ JPA entities” thì cần đổi sang `ddl-auto=create` và tắt Flyway, nhưng sẽ làm schema lệch khỏi migrations và không khuyến nghị cho dự án này.

