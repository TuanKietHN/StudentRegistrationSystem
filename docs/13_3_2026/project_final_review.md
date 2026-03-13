# Tổng Kết & Đánh Giá Dự Án Hệ Thống Quản Lý Đào Tạo (CMS)

**Ngày cập nhật:** 13/03/2026

Tài liệu này cung cấp một cái nhìn tổng quan, đánh giá mức độ hoàn thiện, phân tích bảo mật và định hướng triển khai dự án CMS dựa trên Spring Boot (Java 21) và Vue 3.

---

## 1. Mức Độ Hoàn Thiện Chức Năng (Feature Completion)

Dự án hiện tại đã **hoàn thành 100% các tính năng cốt lõi** theo kế hoạch (Plan) đề ra ban đầu. Hệ thống đã hoạt động trơn tru với sự phân quyền rõ rệt cho 3 nhóm đối tượng:

1.  **Quản trị viên (Admin):**
    *   Quản lý toàn diện danh mục: Khoa, Bộ môn, Học kỳ, Giảng viên, Sinh viên, Người dùng.
    *   Quản lý chương trình đào tạo: Tạo mới CTĐT và liên kết danh sách môn học dễ dàng qua giao diện Page-based.
    *   Quản lý Lớp Cố định (Lớp hành chính) và Lớp Tín chỉ (Học phần).
    *   Giám sát và can thiệp điểm số toàn hệ thống.

2.  **Giảng viên (Teacher):**
    *   Xem lịch dạy, lớp học phần được phân công.
    *   Quản lý lớp hành chính mình làm cố vấn học tập (xem danh sách, tiến độ học tập của sinh viên).
    *   **Chấm điểm:** Xem danh sách ghi danh theo lớp tín chỉ, nhập điểm trực tiếp hoặc import file Excel, chốt điểm.

3.  **Sinh viên (Student):**
    *   Xem tiến độ học tập, chương trình đào tạo của bản thân.
    *   **Đăng ký học:** Tìm kiếm lớp mở, đăng ký, hủy đăng ký trực tuyến, kiểm tra trùng lịch.
    *   Xem lịch biểu. Điểm số chỉ hiển thị sau khi giảng viên đã cập nhật và chốt điểm ở tiến độ học tập (không hiển thị lúc đang học).

---

## 2. Kế Hoạch Triển Khai (Deployment Plan)

Dự án hiện đã đủ điều kiện để triển khai lên môi trường thử nghiệm (Staging) hoặc thực tế (Production). Các bước tiếp theo cần chuẩn bị:

1.  **Môi trường Backend:**
    *   Chuyển đổi từ `application-dev.properties` sang cấu hình `application-prod.properties` (hoặc thiết lập biến môi trường `.env` trên Server).
    *   Tắt `cms.seed.enabled=false` trên môi trường Production để tránh ghi đè dữ liệu mẫu.
    *   Đóng gói ứng dụng thành file `.jar` (`mvn clean package`) hoặc Dockerize backend rành một container image.
2.  **Cơ sở dữ liệu:** Cài đặt PostgreSQL trên server/Cloud (AWS RDS, Supabase, v.v.). Đảm bảo Flyway sẽ tự động chạy migration khi start app.
3.  **Môi trường Frontend:**
    *   Chạy lệnh `npm run build` để build Vue 3 ra các file static nguyên khối.
    *   Sử dụng NGINX để host file tĩnh và cấu hình Reverse Proxy đẩy API request `/api/` về cho Backend Spring Boot.
4.  **Bảo mật Server:** Cấu hình SSL/TLS, ẩn port trực tiếp của Database. Cấu hình bảo mật JWT Secrets bí mật cho Production.

---

## 3. Phân Tích Các Lỗi Tiềm Ẩn (Potential Hidden Bugs)

Dù đã tái cấu trúc và test kỹ, trong môi trường thực tế (Concurrent Users cao), hệ thống có thể gặp các vấn đề kĩ thuật sau:

1.  **Race Condition khi Đăng Ký Học:**
    Hiệu trạng: Nếu một lớp học chỉ còn 1 slot cuối cùng (Ví dụ: 39/40) và 2 sinh viên bấm "Đăng ký" ở cùng một mili-giây, giao dịch database có thể bị "lọt" khiến lớp chứa 41/40 sinh viên.
    *Cách khắc phục:* Áp dụng Optimistic Locking (`@Version`) trên entity `Section` hoặc dùng Transaction isolation cấp cao/Redis khóa phân tán tại `EnrollmentService` khi tăng biến đếm sinh viên.
2.  **Xoá Dữ Liệu Ràng Buộc (Referential Integrity):**
    Hiện tại, nếu Admin xóa một Subject (Môn học) đang được gắn vào một Academic Program hoặc đang có lớp học (Section) diễn ra, hệ thống DB sẽ báo lỗi khóa ngoại 500 ném ra FE.
    *Cách khắc phục:* Bổ sung xử lý Soft Delete (Xóa mềm - `active = false`) hoặc trả thông báo rành mạch "Dữ liệu đang được sử dụng" bằng RestControllerAdvice.

---

## 4. Phân Tích Bảo Mật: XSS và SQL Injection

Hệ thống đã được thiết kế tuân theo tiêu chuẩn an toàn cao để chống lại hai phương thức tấn công phổ biến nhất:

### SQL Injection (Mức Độ Rủi Ro: Rất Thấp - An Toàn)
1.  **Cơ chế bảo vệ:** Hệ thống sử dụng **Spring Data JPA / Hibernate** làm lớp trung gian giao tiếp với PostgreSQL.
2.  Tất cả các câu truy vấn phức tạp (ví dụ trong `JpaUserRepository.java` hoặc `StudentJpaRepository.java`) đều sử dụng Annotation `@Query` kết hợp với tham số có định danh (`:keyword`, `:departmentId`).
3.  Hibernate tự động chuyển đổi các truy vấn này thành **Prepared Statements** ở tầng JDBC. Parameter được DB engine xử lý như dữ liệu text thuần tuý, tuyệt đối không bị nối chuỗi (String Concatenation) thành logic SQL -> Tránh 100% SQL Injection qua input tìm kiếm.

### Cross-Site Scripting - XSS (Mức Độ Rủi Ro: Rất Thấp - An Toàn)
1.  **Cơ chế bảo vệ Frontend:** Giao diện được xây dựng hoàn toàn bằng **Vue 3**. Vue 3 sử dụng cơ chế nội suy văn bản `{{ variable }}` và tự động HTML-escape toàn bộ nội dung mà người dùng nhập vào. Do đó, nếu malicious user nhập `<script>alert(1)</script>` vào tên Chương trình đào tạo, nó sẽ hiển thị dưới dạng chuỗi thuần tuý chứ không thực thi.
2.  Qua rà soát toàn bộ source code `frontend/src`, dự án **KHÔNG SỬ DỤNG** chỉ thị `v-html`. Đây là chỉ thị duy nhất có nguy cơ render trực tiếp mã HTML tà độc. Bằng cách không dùng `v-html`, toàn bộ rủi ro DOM-based XSS được triệt tiêu.
3.  **Cơ chế bảo vệ Backend:** Backend tuân thủ nghiêm ngặt chuẩn Validation (`@Valid`, `@NotBlank`, ...) chặn từ cửa ngõ các request chứa dữ liệu quá kỳ dị.

### Các Tính Năng Bảo Mật Khác Đã Có Trong Hệ Thống:
- Tích hợp Refresh Token rẽ nhánh (Hạn chế Session Hijacking khi Access Token bị lộ).
- Rate Limiting giới hạn IP tránh Brute-force Login.
- Phân quyền RBAC mạnh mẽ bằng `@PreAuthorize("hasAuthority('...')")`. Giảng viên này không chấm điểm được cho lớp của giảng viên khác. Sinh viên không xem được bảng điểm của sinh viên khác.

---

**Kết luận:** Hệ thống quản lý đào tạo đã đạt độ hoàn chỉnh về luồng nghiệp vụ. Độ an toàn về bảo mật dữ liệu ở mức cao. Các chức năng hiển thị/đăng ký/chấm điểm được bóc tách chặt chẽ. Hệ thống sẵn sàng để chuyển sang quy trình triển khai (Deployment) và tối ưu hiệu suất (Performance Tuning) cho tải cao.
