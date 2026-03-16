# Phân tích Luồng Dữ liệu và Sửa lỗi

Tài liệu này phân tích chi tiết luồng dữ liệu từ Frontend xuống Backend cho hai lỗi vừa được khắc phục, bao gồm các biến và phương thức cụ thể tham gia vào quy trình.

---

## 1. Lỗi: Giảng viên không thấy danh sách sinh viên lớp chủ nhiệm
**Hiện tượng:** Giảng viên truy cập vào lớp chủ nhiệm nhưng danh sách sinh viên trống hoặc gây lỗi hệ thống.

### Phân tích Luồng Dữ liệu (Data Flow)

#### A. Frontend (Vue.js)
1.  **View**: `TeacherAdminClassStudentsView.vue`
    *   **Hook**: `onMounted` gọi hàm `loadStudents()`.
    *   **Biến**: `students` (Reactive ref) dùng để hứng dữ liệu trả về.
    *   **Gọi Service**: `studentClassService.getStudents(classId)`.

2.  **Service**: `studentClass.service.ts`
    *   **Method**: `getStudents(id: number)`
    *   **API Path**: `GET /v1/student-classes/{id}/students`
    *   **Nhiệm vụ**: Gửi request HTTP xuống backend.

#### B. Backend (Spring Boot)
1.  **Controller**: `StudentClassController.java`
    *   **Method**: `getStudentClassStudents(@PathVariable Long id)`
    *   **Security**: `@PreAuthorize` kiểm tra xem giáo viên hiện tại có phải là chủ nhiệm (`advisor`) của lớp này không.
    *   **Gọi Service**: `studentClassService.getStudentClassStudents(id)`.

2.  **Service**: `StudentClassServiceImpl.java` (Nơi xảy ra lỗi)
    *   **Method**: `getStudentClassStudents(Long id)`
    *   **Logic cũ (Gây lỗi)**:
        ```java
        List<Student> students = studentRepository.findByStudentClassId(id);
        return students.stream().map(StudentResponse::fromDomain).toList();
        ```
    *   **Nguyên nhân**:
        *   Entity `Student` có quan hệ với `User` (chứa tên, email).
        *   Mặc định JPA sử dụng **Lazy Loading** cho quan hệ này.
        *   Khi `students` được trả về Controller và Jackson thực hiện serialize sang JSON, Transaction database đã đóng.
        *   Khi Jackson cố gắng truy cập `student.getUser().getFullName()`, Hibernate ném lỗi `LazyInitializationException` hoặc trả về `null` khiến dữ liệu Frontend bị thiếu.

### Giải pháp (Fix)
Tại `StudentClassServiceImpl.java`, thêm đoạn mã để **ép buộc tải dữ liệu** (Force Initialization) khi Transaction còn mở:

```java
// Trong method getStudentClassStudents
List<Student> students = studentRepository.findByStudentClassId(id);

// FIX: Truy cập vào thuộc tính User để Hibernate thực hiện query ngay lập tức
students.forEach(s -> {
    if (s.getUser() != null) {
        s.getUser().getId(); // Dòng này kích hoạt Lazy Loading
    }
});

return students.stream().map(StudentResponse::fromDomain).toList();
```

---

## 2. Lỗi: Admin không tạo được Chương trình đào tạo
**Hiện tượng:** Nhấn "Lưu" nhưng báo lỗi hoặc API gọi sai đường dẫn.

### Phân tích Luồng Dữ liệu (Data Flow)

#### A. Frontend (Vue.js)
1.  **View**: `AdminProgramListView.vue`
    *   **Biến Form**: `editedItem` (Reactive object chứa `code`, `name`, `departmentId`, ...).
    *   **Method**: `saveProgram()`
    *   **Lỗi 1 (Validation)**: Trước đây không kiểm tra `departmentId` có giá trị hay không. Nếu `null`, Backend sẽ trả về lỗi 400 Bad Request.
    *   **Lỗi 2 (Data Binding)**: Dropdown chọn Khoa (`v-select`) có thể chưa bind đúng vào `editedItem.departmentId`.

2.  **Service**: `academicProgram.service.ts`
    *   **Method**: `create(data)`
    *   **Lỗi 3 (API Path)**:
        *   Cấu hình `axios` gốc (`http.ts`) đã có `baseURL = /api`.
        *   Code cũ trong service gọi: `axios.post('/api/v1/academic-programs', data)`.
        *   **Kết quả**: URL thực tế trở thành `http://localhost:8080/api/api/v1/academic-programs` -> **404 Not Found**.

#### B. Backend (Spring Boot)
1.  **Controller**: `AcademicProgramController.java`
    *   **API**: `POST /api/v1/academic-programs`
    *   **DTO**: `AcademicProgramCreateRequest`
    *   **Validation**: `@NotNull` trên field `departmentId`. Nếu Frontend gửi `null`, Controller chặn lại ngay lập tức.

### Giải pháp (Fix)

**1. Sửa Frontend View (`AdminProgramListView.vue`)**:
Thêm validation trước khi gọi service:
```javascript
const saveProgram = async () => {
  // FIX: Kiểm tra bắt buộc chọn Khoa
  if (!editedItem.departmentId) {
      uiStore.notify('Vui lòng chọn Khoa', 'error')
      return
  }
  // ... gọi service
}
```

**2. Sửa Frontend Service (`academicProgram.service.ts`)**:
Loại bỏ prefix `/api` thừa:
```typescript
// Cũ (Sai): axios.post('/api/v1/academic-programs', data)
// Mới (Đúng):
create: async (data: AcademicProgramCreateRequest) => {
    const response = await axios.post('/v1/academic-programs', data)
    return response.data
},
```

---

### Tổng kết
| Chức năng | Vị trí Lỗi | Nguyên nhân Kỹ thuật | Cách sửa |
| :--- | :--- | :--- | :--- |
| **Teacher Student List** | Backend Service | **Lazy Loading**: Session đóng trước khi lấy dữ liệu User. | Gọi `getUser().getId()` trong Transaction. |
| **Create Program** | Frontend Service | **Double URL**: `/api/api/...` do thừa prefix. | Xóa `/api` trong file service. |
| **Create Program** | Frontend View | **Missing Payload**: Thiếu validation `departmentId`. | Thêm check `if (!departmentId)`. |
