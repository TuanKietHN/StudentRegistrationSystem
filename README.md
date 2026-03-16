# Hệ thống Quản lý Khóa học / Sinh viên (Course Management System)

Dự án Hệ thống Quản lý Khóa học được thiết kế và xây dựng dựa trên kiến trúc **Modular Monolith** sử dụng nền tảng **Spring Boot** ở Backend và **Vue.js** ở Frontend.

## 🛠️ Công nghệ sử dụng
- **Backend**: Java 21, Spring Boot, Spring Data JPA, Hibernate REST API
- **Frontend**: Vue.js, Vue Router, Vuex, Vite
- **Database**: PostgreSQL
- **Database Migration**: Flyway
- **Công cụ build**: Maven (Maven Wrapper)

## 📋 Yêu cầu hệ thống (Prerequisites)
Để có thể chạy được dự án, máy tính của bạn cần cài đặt:
- **Java Development Kit (JDK) 21**
- **PostgreSQL** đang hoạt động (mặc định tại cổng `5432`)
- **Node.js** và **npm** (để khởi chạy Frontend)

---

## 🚀 Hướng dẫn cài đặt và khởi chạy dự án

### 1. Cấu hình và Migrate Database (Flyway)
**⚠️ QUAN TRỌNG:** Trước khi khởi chạy ứng dụng (chạy Backend), bạn **bắt buộc** phải sử dụng Flyway để cấp phát các bảng (tables) và dữ liệu mẫu (seed data) vào trong hệ quản trị cơ sở dữ liệu PostgreSQL.

Mở Terminal / Command Prompt tại thư mục ROOT của dự án và chạy câu lệnh dưới đây. Vui lòng thay thế `<tên-database>`, `<tên-user-postgre>`, và `<password-postgre>` thành cấu hình chuẩn trên máy của bạn:

```bash
./mvnw "org.flywaydb:flyway-maven-plugin:10.10.0:migrate" "-Dflyway.url=jdbc:postgresql://localhost:5432/<tên-database>" "-Dflyway.user=<tên-user-postgre>" "-Dflyway.password=<password-postgre>" "-Dflyway.locations=filesystem:src/main/resources/db/migration"
```

*Đảm bảo Database của bạn đã được tạo sẵn trong PostgreSQL trước khi chạy câu lệnh migrate trên.*

### 2. Khởi chạy Backend (Spring Boot)
Sau khi Flyway migrate database hoàn tất thành công, bạn có thể khởi động server Backend bằng lệnh:

```bash
./mvnw spring-boot:run
```
*(Hoặc thưc hiện chạy trực tiếp file application chính thông qua IDE).*
API Backend theo mặc định sẽ được chạy tại cổng `http://localhost:8080`.

#### 🛠️ Xử lý lỗi build (Nếu gặp lỗi)
Trong trường hợp dự án gặp lỗi biên dịch hoặc lỗi phụ thuộc (dependency), bạn có thể thử làm sạch và build lại dự án bằng các lệnh sau:

- **Làm sạch và biên dịch:**
  ```bash
  ./mvnw clean compile
  ```
- **Làm sạch và đóng gói ứng dụng:**
  ```bash
  ./mvnw clean package
  ```

### 3. Khởi chạy Frontend (Vue.js)
Mở một cửa sổ Terminal mới, đi vào thư mục frontend, tiến hành cài đặt các dependencies và chạy dự án phía máy khách:

```bash
cd frontend
npm install
npm run dev
```
Sau đó, truy cập vào đường dẫn được cung cấp ở Terminal (thường là `http://localhost:5173` hoặc tương tự) bằng trình duyệt web để sử dụng hệ thống.

---

## 📂 Kiến trúc & Tổ chức mã nguồn
Dự án được xây dựng theo kiến trúc **Modular Monolith**, gom nhóm các chức năng (như `academic`, `auth`...) thành các module độc lập bên trong cùng một project. Ưu điểm là phân tách ranh giới logic rõ ràng mà không phải chịu tổn phí quản lý phức tạp như Microservices.

- **`src/main/java/vn/com/nws/cms/modules/`**: Nơi chứa các thư mục module cốt lõi. Mỗi module sẽ có cấu trúc layer chuẩn (API, Core/Domain, Infrastructure).
- **`src/main/resources/db/migration/`**: Nơi lưu trữ các version script `.sql` của Flyway phụ trách thiết lập database.
- **`frontend/`**: Chứa toàn bộ giao diện phía người dùng.
