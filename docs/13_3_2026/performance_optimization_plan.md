# Kế Hoạch Tối Ưu Hiệu Suất (Performance Optimization Plan)

**Ngày cập nhật:** 13/03/2026

Tài liệu này xác định các nút thắt hiệu suất tiềm ẩn trong hệ thống CMS hiện tại và đề xuất các biện pháp kỹ thuật chuyên sâu để hệ thống có khả năng mở rộng (Scalability) và chịu tải cao (High Concurrency) trong giai đoạn Production.

---

## 1. Nút Thắt Hiệu Suất & Giải Pháp (Bottlenecks & Solutions)

### 1.1. Race Condition Ở Phân Hệ Đăng Ký Học
*Vấn đề:* Module mở lớp tín chỉ (Section) có số lượng sinh viên tối đa (`maxStudents`). Trong đợt cao điểm đăng ký toàn trường, số lượng request đổ về `POST /api/v1/sections/{id}/enroll` là cực lớn. Database sẽ bị quá tải khóa dòng hoặc bị Request cạnh tranh đè kết quả.
*Giải Pháp Cấp 1 (Lập trình):* Dùng `@Lock(LockModeType.PESSIMISTIC_WRITE)` trên JPA Repository đối với query `findById(sectionId)` hoặc thêm cột `@Version` (Optimistic Locking).
*Giải Pháp Cấp 2 (Kiến trúc - Queueing):* 
- Sử dụng **RabbitMQ / Redis Queue** đã cấu hình sẵn trong dự án. 
- API Request gửi thẳng 1 message (Event) gồm `studentId` & `sectionId` vào hàng đợi. Consumer sẽ lần lượt pop-out để xử lý chốt Slot vào DB, điều hòa tốc độ (Throttling) Database xuống còn 100-200 tx/sec.

### 1.2. Màn Hình Xem Danh Sách / Tìm Kiếm Nặng
*Vấn đề:* Bảng `students`, `enrollments` sẽ phình to rất nhanh (hàng trăm ngàn records). Các API GET ALL chứa `LIKE %keyword%` sẽ quét toàn bộ bảng (Full Stack Scan) gây thắt cổ chai DB.
*Giải Pháp:*
- Áp dụng **B-Tree Index** cho các cột tìm kiếm thường xuyên như `student_code`, `code`, `email`, `username`. 
- Cân nhắc sử dụng **ElasticSearch** hoặc **PostgreSQL Trigram (pg_trgm)** cho tác vụ tìm kiếm Full-Text diện rộng.

### 1.3. Vấn Đề N+1 Truy Vấn của Hibernate
*Vấn đề:* Hibernate gọi 1 query lấy danh sách Parent và N query rời rạc cho vòng lặp lấy từng Child Entity. (Lazy fetching không tối ưu).
*Giải Pháp:* 
- Thêm FETCH JOIN vào Annotation Của Repository (ví dụ: `LEFT JOIN FETCH e.section s LEFT JOIN FETCH s.subject`).
- Áp dụng Entity Graphs (`@EntityGraph`) lên các method Repository trọng yếu để đảm bảo chỉ sinh 1 câu lệnh JOINS SQL lên DB.

---

## 2. Các Tầng Bộ Nhớ Đệm (Caching Layers)

Hệ thống cần áp dụng Cache 3 lới (3-layer caching) để giảm tải xuống CSDL:

1. **Spring Cache / Redis (Backend):**
   - Đặt `@Cacheable(value = "academic_programs")` hoặc `@Cacheable(value = "departments")` cho các dữ liệu Danh mục (Master Data) hiếm khi thay đổi.
   - Các API List Department / List Subject có thể lưu ở RAM (Redis) trong 1-2 tiếng, giúp Time-to-First-Byte (TTFB) giảm xuống < 10ms.

2. **L2 Cache của Hibernate (Database):**
   - Bật Hibernate Level 2 Cache bằng EhCache / Hazelcast để cache trực tiếp các Entity Model (`@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)`).

3. **HTTP Caching / Service Worker (Frontend):**
   - Cấu hình NGINX trả về Header `Cache-Control: public, max-age=31536000` cho tất cả file JS/CSS/Fonts/Ảnh tĩnh tịnh tiến.
   - Thêm Etag hoặc If-Modified-Since trên những Response API thường xuyên để Client không tải lại dữ liệu tĩnh.

---

## 3. Tối Ưu Kết Nối và Máy Chủ (Infrastructure Optimization)

1. **Cấu Hình Hồ Bơi Kết Nối (Connection Pooling):** 
   - HikariCP (Mặc định của Spring Boot) cần tinh chỉnh `maximum-pool-size = 20-50` tuỳ cấu hình Core của Server. Không để quá lớn gây nghẽn RAM DB.

2. **Bất Đồng Bộ Logging (Asynchronous Logging):**
   - Chuyển cấu hình thư viện Logback của Spring Boot sang `<appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">`. Khóa Logger không chặn luồng chạy thực của ứng dụng.

3. **Frontend Bundle Phân Tách Nghề Môn (Code-splitting):**
   - Tại Vite, đã tiến hành Lazy Load Module (Dấu `import()` trong bảng route `router/index.ts`). Tuy nhiên, cần kiểm tra kích thước Bundle. Nếu thấy Vendor size lớn, cấu hình `manualChunks` tách Vue, Vuetify và Axios thành file js độc lập, để trình duyệt có thể Cache vĩnh viễn những Tool này.

### Tổng Kết
Việc Tối ưu sẽ là một tiến trình liên tục dựa trên Metric giám sát (Bằng Prometheus + Grafana). Ưu tiên thiết lập Queue ở tính năng Đăng ký học trước để phòng ngừa sập hệ thống ngày "mở cổng tín chỉ".
