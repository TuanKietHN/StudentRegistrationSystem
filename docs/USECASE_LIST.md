# Danh sách Usecase Hiện tại của Hệ thống CMS

Dưới đây là danh sách các usecase hiện có trong hệ thống, được phân loại theo các module chính.

## 1. Authentication & IAM (Quản lý Xác thực & Người dùng)
- **Đăng ký tài khoản**: Người dùng mới đăng ký qua email/username.
- **Đăng nhập**: Xác thực bằng username/password, cấp Access Token (JWT) và Refresh Token (Cookie HttpOnly).
- **Làm mới Token (Refresh Token Rotation)**: Tự động cấp mới Access Token khi hết hạn; phát hiện reuse để khóa tài khoản.
- **Đăng xuất**: Vô hiệu hóa Refresh Token và phiên làm việc trong Redis.
- **Quên/Đặt lại mật khẩu**: Gửi email hướng dẫn và mã đặt lại mật khẩu.
- **Quản lý phiên làm việc**: Xem và thu hồi các phiên đăng nhập từ các thiết bị khác nhau.
- **Phân quyền dựa trên Vai trò (RBAC)**: Kiểm soát truy cập dựa trên các vai trò: ADMIN, TEACHER, STUDENT.
- **Chuyển đổi vai trò (Role Switcher)**: Cho phép người dùng có nhiều vai trò chuyển đổi ngữ cảnh làm việc (ví dụ: vừa là Teacher vừa là Student).
- **Audit Log**: Ghi lại lịch sử các sự kiện bảo mật (đăng nhập, logout, đổi mật khẩu).

## 2. Quản lý Đào tạo (Academic Management)
- **Quản lý Khoa/Phòng ban (Department)**: CRUD các khoa trong trường.
- **Quản lý Giảng viên (Teacher)**: Danh sách và thông tin chi tiết giảng viên.
- **Quản lý Môn học (Subject)**: Định nghĩa các môn học, số tín chỉ, và trọng số điểm.
- **Quản lý Học kỳ (Semester)**: Quản lý các kỳ học và trạng thái hoạt động.
- **Quản lý Khóa học (Cohort)**: Quản lý các khóa tuyển sinh (ví dụ: K20, K21).
- **Quản lý Lớp chuyên ngành (Admin Class)**: Quản lý các lớp cố định của sinh viên theo khóa.

## 3. Đăng ký & Học tập (Student & Enrollment)
- **Đăng ký môn học**: Sinh viên đăng ký vào các lớp học phần (Section) trong kỳ học.
- **Quản lý ghi danh (Enrollment)**: Giảng viên hoặc Admin duyệt/quản lý danh sách sinh viên trong lớp học phần.
- **Xem thời khóa biểu**: Hiển thị lịch học cho sinh viên và lịch dạy cho giảng viên.
- **Quản lý sinh viên trong lớp**: Xem danh sách sinh viên thuộc một lớp chuyên ngành hoặc lớp học phần.

## 4. Hệ thống (System)
- **Quản lý File (MinIO)**: Lưu trữ và quản lý tài liệu, hình ảnh.
- **Messaging (RabbitMQ)**: Gửi thông báo email bất đồng bộ.
- **Caching (Redis)**: Tối ưu hóa hiệu năng và quản lý phiên làm việc.
- **Tài liệu API (Swagger)**: Tự động tạo tài liệu cho các endpoint API.
