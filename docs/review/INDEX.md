# Review dự án CMS (2026-02)

Mục tiêu của bộ tài liệu này:
- Rà soát hiện trạng codebase (backend + frontend)
- Đối chiếu cấu trúc database (migration) với entity/domain hiện có
- Đánh giá mức phù hợp cho hệ thống quản lý khóa học nội bộ
- Đề xuất các chức năng bổ sung theo hướng quản lý nội bộ (không bao gồm LMS/online)

## Danh mục

1. [01-Database.md](01-Database.md)  
   Hiện trạng schema theo migrations V1–V8, những bảng đang được code sử dụng, và các điểm lệch giữa schema–entity.

2. [02-Domain-Entity.md](02-Domain-Entity.md)  
   Đánh giá domain/entity theo góc nhìn “quản lý khóa học nội bộ”, các lệch khái niệm và đề xuất chuẩn hoá.

3. [03-Features-Gaps.md](03-Features-Gaps.md)  
   Tổng hợp chức năng hiện có (API/role) và danh sách tính năng có thể bổ sung theo mức ưu tiên.

4. [04-Video-Extension.md](04-Video-Extension.md)  
   Ghi chú phạm vi: loại bỏ khóa học online/video; chuẩn hoá Course theo V1 (lớp học phần).
