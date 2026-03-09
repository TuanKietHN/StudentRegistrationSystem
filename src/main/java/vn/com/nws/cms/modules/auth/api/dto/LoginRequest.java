package vn.com.nws.cms.modules.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Thông tin yêu cầu đăng nhập")
public class LoginRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Schema(description = "Tên tài khoản người dùng", example = "admin")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Schema(description = "Mật khẩu truy cập", example = "P@ssword123")
    private String password;

    public void setUsername(String username) {
        this.username = username != null ? username.trim() : null;
    }

    public void setPassword(String password) {
        this.password = password != null ? password.trim() : null;
    }
}
