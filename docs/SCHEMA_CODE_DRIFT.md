# So sánh schema (Flyway) vs code (JPA entities)

Phạm vi so sánh:
- Schema: [V1__init_schema.sql](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/resources/db/migration/V1__init_schema.sql)
- Code: các JPA entities trong `src/main/java/**/infrastructure/persistence/entity`

Mục tiêu: liệt kê các điểm “lệch” để DB có thể được tạo mới (reset) theo schema hiện tại và seeder chạy đúng theo code.

## 1) Các bảng có trong schema nhưng code không dùng (không có entity/repository)

Các bảng dưới đây được tạo trong schema nhưng hiện không có entity/repository sử dụng trong code:
- `permissions`
- `role_permissions`
- `refresh_tokens`
- `password_reset_tokens`
- View `v_courses_with_teacher` (code không query view)

Tác động:
- Không làm hỏng JPA trực tiếp, nhưng làm schema “phình” và dễ gây hiểu nhầm khi debug drift.

Khuyến nghị:
- Nếu muốn schema đúng “code-first” theo nghĩa “chỉ tạo những gì code dùng”, nên bỏ các bảng/view này khỏi schema Flyway.

## 2) Các cột có trong schema nhưng không được map trong entity

- `subjects.prerequisite_subject_ids` (BIGINT[])
  - Schema có, nhưng `SubjectEntity` không map.
  - Tác động: không làm JPA fail, nhưng cột sẽ luôn null và không được quản lý bởi code.

Khuyến nghị:
- Nếu cần tính năng môn tiên quyết: implement mapping (custom type) và service.
- Nếu không cần: remove khỏi schema.

## 3) Điểm drift thực tế gây lỗi seed: courses.credits

Triệu chứng:
- DB thực tế của bạn có `courses.credits` NOT NULL, trong khi code hiện tại (JPA) không có field `credits` trong `CourseEntity`.
- Khi JPA insert course, nó không set `credits` → DB nhận NULL → fail `NOT NULL`.

Nguyên nhân:
- DB drift (đã từng có migration/DDL cũ thêm `credits`, hoặc chỉnh tay).
- Hiện schema Flyway đang là “final state” không chứa `courses.credits`, nên nếu reset DB theo Flyway thì drift này biến mất.

Fix theo ưu tiên “code hơn database”:
- Reset DB theo Flyway để tạo schema đúng theo code.
- Ngoài ra (để không bị kẹt khi chưa reset), seeder có thể auto-drop cột `courses.credits` nếu phát hiện tồn tại.

## 4) Kết luận

- Nếu mục tiêu là “tạo database từ code”: chuẩn nhất là **reset DB** và để Flyway migrate từ file schema hiện tại.
- Nếu vẫn muốn chạy trên DB đã drift: cần cơ chế auto-fix (dev-only) để dọn các cột/bảng lệch như `courses.credits`.

