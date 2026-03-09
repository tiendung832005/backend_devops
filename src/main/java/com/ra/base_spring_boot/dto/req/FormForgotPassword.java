package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FormForgotPassword {
    private String email;
    @AssertTrue(message = "Email hoặc Phone không được để trống")
    public boolean hasEmailOrPhone() {
        return email != null && !email.isBlank();

    }

    @AssertTrue(message = "Email không đúng định dạng")
    public boolean isEmailValid() {
        if (email == null || email.isBlank()) return true;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}

