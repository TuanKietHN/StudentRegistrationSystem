# 04. Phạm vi: không triển khai khóa học online/video

Dự án được chuẩn hoá theo hướng **quản lý khóa học nội bộ** và coi `courses` là **lớp học phần** theo V1.

Vì vậy:
- Các migrations về lesson/assessment/attendance (V4–V6) được coi là ngoài phạm vi và đã được loại bỏ khỏi schema bằng [V9__remove_online_learning_schema.sql](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/resources/db/migration/V9__remove_online_learning_schema.sql).
- Các trường “e-learning/marketing” thêm vào `courses` ở V3 được loại bỏ để quay về mô hình V1 bằng [V10__normalize_courses_as_course_sections.sql](file:///c:/Users/Admin/Downloads/Repo/cms/src/main/resources/db/migration/V10__normalize_courses_as_course_sections.sql).

Nếu sau này cần LMS/video:
- Nên tách thành hệ thống riêng (service riêng + schema riêng) để không làm phình core domain quản lý nội bộ.
