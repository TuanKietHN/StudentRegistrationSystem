# Hướng Dẫn Tích Hợp Soft Delete (Xóa Mềm) Toàn Hệ Thống

**Ngày cập nhật:** 13/03/2026

Tài liệu này hướng dẫn cách áp dụng cơ chế Soft Delete (Xóa Mềm) cho toàn bộ hệ thống CMS để giải quyết triệt để lỗi Ràng buộc khóa ngoại (Referential Integrity Constraint) khi xóa các thực thể có liên kết, đồng thời giúp giữ lại lịch sử dữ liệu quan trọng.

---

## 1. Vấn Đề Hiện Tại

Khi sử dụng Hard Delete (`repository.deleteById(id)`), hệ thống sẽ gặp lỗi `DataIntegrityViolationException` chặn lại nếu bản ghi đang xóa được tham chiếu bởi một bản ghi khác.
**Ví dụ:** Xóa một `Subject` (Môn học) đang được tham chiếu trong `AcademicProgram` hoặc `Section`. Thay vì xóa hẳn, ta chỉ đánh dấu trạng thái "đã xóa" (ví dụ: `active = false` hoặc `deleted = true`), giúp dữ liệu lịch sử không bị đứt gãy.

Hệ thống CMS hiện tại đã có sẵn trường `active` (boolean) trong đa số các Entity kế thừa từ Base/AuditEntity. Ta sẽ tận dụng trường này, kết hợp với các công cụ của Hibernate để biến mọi lời gọi "Xóa" thành "Cập nhật trạng thái".

---

## 2. Giải Pháp: Sử dụng Hibernate `@SQLDelete` và `@Where`

Để triển khai đồng bộ ở tầng ORM mà không phải rà soát, sửa đổi từng Service một (Không can thiệp logic Code Service), ta dùng sức mạnh của Spring Data JPA / Hibernate annotation.

### Bước 2.1: Bổ Sung Annotation Tại Lớp Entity

Mở các Entity cốt lõi đang cần Soft Delete (VD: `SubjectEntity`, `DepartmentEntity`, `TeacherEntity`, `AcademicProgramEntity`...) và thêm 2 annotation ở đầu mức class:

*Lưu ý:* Cấu trúc DB hiện tại có cột `active`. Nếu quy ước `active = false` là xóa mềm:

```java
@Entity
@Table(name = "subjects")
// 1. Chặn lệnh DELETE vật lý, thay bằng UPDATE
@SQLDelete(sql = "UPDATE subjects SET active = false WHERE id = ?")
// 2. Tự động bỏ qua các bản ghi đã xóa ở mọi câu lệnh SELECT (tuỳ chọn)
@Where(clause = "active = true") 
public class SubjectEntity extends AuditEntity {
    // ... props
    private boolean active;
}
```

**Ưu điểm:** Bất kỳ thao tác `subjectRepository.deleteById(id)` nào ở tầng Service sẽ được biến đổi ngầm thành lệnh `UPDATE ... SET active = false WHERE id=?`.

### Bước 2.2: Xử Lý Annotation Dừng Lại (Deprecation của `@Where`)

Từ phiên bản Hibernate 6.3 trở đi (nếu Spring Boot 3.2+ chạy Java 21 dùng bản mới), `@Where` đã bị @Deprecated và thay thế bằng `@SQLRestriction`.
Cập nhật cho Hibernate mới nhất:
```java
@Entity
@Table(name = "subjects")
@SQLDelete(sql = "UPDATE subjects SET active = false WHERE id = ?")
@SQLRestriction("active = true") // Thay thế cho @Where
public class SubjectEntity extends AuditEntity { ... }
```

### Bước 2.3: Quản Lý Các Relationship Bị Xóa Mềm

Với một số Entity lồng ghép (Parent - Child), nếu Parent bị xóa mềm, hệ thống sẽ tự động cập nhật lại các màn hình Select list. Trạng thái `active = true` sẽ loại bỏ các phần tử Child hiển thị trong Dropdown trên Frontend.
*Lưu ý quan trọng:* Đối với các bản ghi liên kết trực tiếp như Điểm lịch sử (Grades) của Sinh viên, vẫn giữ nguyên được việc join bảng vì cơ sở dữ liệu vật lý (PostgreSQL) không hề mất hàng. Nếu bạn gặp lỗi truy vấn dữ liệu cũ bị `@SQLRestriction` ẩn mất, hãy cân nhắc sử dụng Native Query hoặc gỡ `@SQLRestriction` ở Entity đó, chỉ tự xử lý điều kiện "Lọc danh sách chọn active=true" ở tầng Service.

---

## 3. Lựa chọn Soft Delete theo Base Entity (Khuyến nghị)

Do dự án đã có `AuditEntity.java`, ta có thể xem xét thêm một cờ chung tên `is_deleted` vào đó để không làm nhiễu ý nghĩa của cột `active` (Vốn dĩ `active` dùng để "Tạm ngưng" hoạt động, việc "Xóa hẳn vào thùng rác" nên là cột `deleted`).

1. Bổ sung trường vào cơ sở dữ liệu bằng Flyway (`VX__add_deleted_column.sql`):
   ```sql
   ALTER TABLE departments ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
   ALTER TABLE subjects ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
   -- ... làm tương tự với các entity khác
   ```

2. Bổ sung thuộc tính vào `AuditEntity.java`:
   ```java
   @Column(name = "is_deleted", nullable = false)
   private boolean isDeleted = false;
   ```

3. Thêm Annotation vào các Entity tương ứng:
   ```java
   @SQLDelete(sql = "UPDATE my_table SET is_deleted = true WHERE id = ?")
   @SQLRestriction("is_deleted = false")
   ```

---

## 4. Kế Hoạch Triển Khai Thực Tế

1. **Giai đoạn 1**: Áp dụng lên các Entity gốc (Department, Subject, AcademicProgram, Semester).
2. **Giai đoạn 2**: Áp dụng lên các Entity con người (User, Student, Teacher). Tuy nhiên, cần cân nhắc gỡ Unique Constraint `email`/`username` để sau này không bị lỗi tạo tài khoản mới đụng tài khoản cũ đã bị Xoá Mềm. (Hoặc rename username lúc xóa: `username_deleted_123`).
3. **Giai đoạn 3**: Test lại tất cả các thao tác GET All ở API. Khẳng định Frontend không còn nhìn thấy các Items đã bị xoá.

> Bằng cách áp dụng kiến trúc chuẩn này, 80% effort refactor code Service được giảm tải chỉ thông qua Annotations mạnh mẽ của Hibernate.
