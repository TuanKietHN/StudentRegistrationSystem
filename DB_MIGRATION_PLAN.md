# Database Migration & Update Plan

## Objective
Chuẩn hoá hệ thống theo các quyết định:
- `courses` là **lớp học phần** theo V1 (không triển khai LMS/online).
- Teacher là **profile tuỳ chọn** của `users` (User + Optional Role Profile).
- Quy về **1 nguồn schema là Flyway** (JPA chỉ validate).

## Scope
- Giữ lại: Auth/IAM (RBAC), Academic core (subjects/semesters/courses/enrollments), departments/teachers profiles.
- Loại bỏ khỏi schema: lesson/assessment/attendance (V4–V6).

## Thay đổi migration (đã/đang áp dụng)
1. V9: drop toàn bộ schema LMS/online + views/functions liên quan.
2. V10: bỏ các cột “e-learning oriented” khỏi `courses`, giữ `courses` theo V1; sửa view `v_courses_with_teacher` theo `teacher_id = users.id`.
3. V11: hợp nhất `subjects.credit` → `subjects.credits` và drop `credit`.

## Chuẩn hoá seeding strategy
Nguyên tắc:
- Flyway seed: dữ liệu nền tảng, deterministic, có thể chạy lại an toàn (`ON CONFLICT DO NOTHING`).
- Application seeder: chỉ cho dev/demo, được bật qua profile/property, và idempotent.

## Cấu hình runtime (bắt buộc)
- `spring.flyway.enabled=true`
- `spring.jpa.hibernate.ddl-auto=validate`
