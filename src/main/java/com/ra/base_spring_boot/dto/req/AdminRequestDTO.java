package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.model.constants.StatusUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class AdminRequestDTO {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Username is required")
    private String username;

    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String avatarUrl;
    private String bio;

    @NotNull(message = "Roles are required")
    private Set<Long> roleIds;

    private StatusUser status = StatusUser.ACTIVE;
}
