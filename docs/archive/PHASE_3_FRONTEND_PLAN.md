# Kế Hoạch Triển Khai Giai Đoạn 3: Frontend (Vue.js)

## 1. Mục Tiêu
Xây dựng giao diện người dùng để tương tác với các API Backend đã phát triển ở giai đoạn trước.
- **Admin**: Quản lý chương trình đào tạo (CRUD, thêm môn học).
- **Teacher**: Xem tiến độ học tập của sinh viên lớp chủ nhiệm.
- **Student**: Xem tiến độ học tập của bản thân (Dashboard).

## 2. Các Công Việc Cần Thực Hiện

### 2.1. Service Layer (Frontend)
- Tạo `frontend/src/api/services/academicProgram.service.ts`:
  - Gọi API `/api/v1/academic-programs` (CRUD).
  - Gọi API `/api/v1/academic-programs/{id}/subjects`.
- Tạo `frontend/src/api/services/studentProgress.service.ts`:
  - Gọi API `/api/v1/student-progress/{studentId}`.

### 2.2. Admin Views (Quản lý chương trình)
- **`AdminProgramListView.vue`**: Danh sách chương trình đào tạo.
- **`AdminProgramDetailView.vue`**: 
  - Chi tiết chương trình.
  - Quản lý danh sách môn học trong chương trình (Thêm/Xóa môn, Sửa điểm đạt).

### 2.3. Student Views (Xem tiến độ)
- **`StudentProgressView.vue`**:
  - Hiển thị thanh tiến độ (Progress Bar).
  - Hiển thị GPA (hệ 10 & 4).
  - Danh sách môn học phân loại theo trạng thái (Đã qua, Chưa qua, Chưa học).

### 2.4. Teacher Views (Xem tiến độ sinh viên)
- Cập nhật **`TeacherAdminClassStudentsView.vue`**:
  - Thêm cột/nút "Xem tiến độ" vào danh sách sinh viên.
  - Khi click sẽ mở dialog hoặc chuyển trang hiển thị component tiến độ (tái sử dụng component của Student).

### 2.5. Routing
- Cập nhật `frontend/src/router/index.ts` để thêm các route mới.

## 3. Chi Tiết Component

### 3.1. `StudentProgressComponent.vue` (Shared Component)
Component dùng chung cho cả Student và Teacher để hiển thị tiến độ.
- **Props**: `studentId` (bắt buộc).
- **UI**:
  - Card tổng quan: % hoàn thành, GPA, Tổng tín chỉ.
  - Table danh sách môn học: Mã môn, Tên môn, Tín chỉ, Điểm, Trạng thái (Color coded).

### 3.2. `ProgramSubjectDialog.vue`
Dialog để Admin thêm môn học vào chương trình.
- Form: Chọn môn học (Autocomplete), Học kỳ, Loại môn (Bắt buộc/Tự chọn), Điểm đạt.

## 4. Thứ Tự Triển Khai
1. Tạo Service API.
2. Xây dựng Shared Component `StudentProgressComponent`.
3. Tích hợp vào View của Student (`StudentProgressView`).
4. Tích hợp vào View của Teacher (`TeacherAdminClassStudentsView`).
5. Xây dựng View quản lý chương trình cho Admin (`AdminProgramListView`, `AdminProgramDetailView`).
6. Cấu hình Router.
