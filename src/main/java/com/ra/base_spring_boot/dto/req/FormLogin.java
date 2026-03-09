package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FormLogin {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String login;
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
