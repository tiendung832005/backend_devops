package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.model.constants.StatusUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberUpdateDTO {
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Email(message = "Email must be valid")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String avatarUrl;
    private String bio;
    private StatusUser status;
}

