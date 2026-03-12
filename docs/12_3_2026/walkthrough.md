# Walkthrough: Refactor Tính năng Tiến trình học tập & Chương trình đào tạo

Tôi đã hoàn thành việc refactor toàn diện tính năng "Tiến trình học tập" để chuyển đổi sang mô hình hiển thị theo **Lộ trình Chương trình đào tạo (8 học kỳ)**.

## Các thay đổi chính

### 1. Backend & Database (DataSeeder)
- **Làm giàu dữ liệu**: Cập nhật `DataSeeder.java` với hàng chục môn học mới và thiết lập khung chương trình đào tạo đầy đủ (60-120 tín chỉ) cho các khóa từ K22 đến K25.
- **Logic lấy dữ liệu (Service)**: Thay đổi `StudentProgressServiceImpl.java` để lấy **Chương trình đào tạo (AcademicProgram)** làm gốc. 
    - Hiển thị tất cả các môn trong chương trình ngay cả khi sinh viên chưa đăng ký học.
    - Phân loại môn học theo từng học kỳ (1-8).
    - Hỗ trợ hiển thị các môn học "ngoài chương trình" (Extra subjects).
- **DTO**: Cập nhật `SubjectProgressDTO` để bao gồm thông tin `semester`, `isExtra`, và phân loại `COMPULSORY / ELECTIVE`.

### 2. Frontend (Giao diện mới)
- **Thiết kế Panel Học kỳ**: Thay đổi từ danh sách phẳng sang giao diện Expansion Panels, giúp sinh viên dễ dàng theo dõi lộ trình theo từng kỳ học.
- **Màu sắc & Trạng thái**:
    - <span style="color: green">●</span> **Đã đạt**: Môn học đã hoàn thành với điểm số.
    - <span style="color: orange">●</span> **Đang học**: Môn học đang trong quá trình đăng ký/học.
    - <span style="color: lightgrey">●</span> **Chưa học**: Môn học trong khung chương trình nhưng chưa đăng ký.
- **Tab chuyển đổi**: Hỗ trợ chuyển đổi giữa xem theo "Khung chương trình" và "Bảng điểm chi tiết".

### 3. Sửa lỗi hiển thị (Bug Fix)
- **ApiResponse Wrapper**: Khắc phục lỗi `StudentProgressController` trả về dữ liệu thô (raw object) thay vì bọc trong `ApiResponse<T>`. Đây là nguyên nhân chính khiến Frontend không nhận diện được dữ liệu và hiển thị lỗi "Không tìm thấy thông tin".
- **Dọn dẹp mã nguồn**: Rà soát và loại bỏ các import dư thừa, đồng thời giữ lại các DTO cũ nhưng vẫn đang được dùng cho các tính năng khác (như xem điểm theo lớp hành chính của giảng viên).

## Kết quả đạt được
- Khắc phục hoàn toàn lỗi "Không tìm thấy thông tin" sau khi cập nhật cấu trúc API.
- Cung cấp cái nhìn trực quan về lộ trình học tập trọn khóa cho sinh viên.
- Tính toán GPA chính xác (Hệ 10 và Hệ 4) dựa trên tất cả các môn đã có điểm.

## Hướng dẫn nghiệm thu
1. **Chạy lại DataSeeder**: Bạn cần chạy lại ứng dụng hoặc kích hoạt seeder để nạp dữ liệu CTĐT mới vào Database.
2. **Đăng nhập Student**: Truy cập mục "Tiến trình học tập" để xem giao diện mới.

> [!IMPORTANT]
> Dữ liệu mẫu đã được thiết lập cho các khóa K22 (ra trường), K23 (năm 3), K24 (năm 2), K25 (năm 1) để đảm bảo bạn thấy được các trạng thái khác nhau của tiến độ.

---
*Tình trạng: Hoàn thành*
