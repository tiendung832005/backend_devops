package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FormRegister {
    private String email;
    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 3, max = 50, message = "Họ tên phải từ 3 - 50 ký tự")
    private String fullName;

    @NotBlank(message = "Tên người dùng không được để trống")
    @Size(min = 4, max = 20, message = "Tên người dùng phải từ 4 - 20 ký tự")
    private String username;

    @AssertTrue(message = "Email hoặc Phone không được để trống")
    public boolean hasEmailOrPhone() {
        return email != null && !email.isBlank();
    }

    @AssertTrue(message = "Email không đúng định dạng")
    public boolean isEmailValid() {
        if (email == null || email.isBlank()) return true;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private String bio;

    private String avatarUrl;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, message = "Password phải có ít nhất 8 ký tự")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password phải chứa chữ hoa, chữ thường, số và ký tự đặc biệt"
    )
    private String password;
}
