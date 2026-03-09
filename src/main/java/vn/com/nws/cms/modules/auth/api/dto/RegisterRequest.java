package vn.com.nws.cms.modules.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Thông tin yêu cầu đăng ký tài khoản")
public class RegisterRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 20, message = "Tên đăng nhập phải từ 3 đến 20 ký tự")
    @Schema(description = "Tên tài khoản mong muốn", example = "giangvien01")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Địa chỉ email không đúng định dạng")
    @Schema(description = "Email liên hệ", example = "teacher@nws.com.vn")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    @Schema(description = "Mật khẩu bảo mật", example = "SecurePass123!")
    private String password;
    
    @Schema(description = "Vai trò người dùng (STUDENT hoặc TEACHER). Mặc định là STUDENT nếu để trống", example = "TEACHER")
    private String role;

    public void setUsername(String username) {
        this.username = username != null ? username.trim() : null;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }

    public void setPassword(String password) {
        this.password = password != null ? password.trim() : null;
    }

    public void setRole(String role) {
        this.role = role != null ? role.trim() : null;
    }
}
