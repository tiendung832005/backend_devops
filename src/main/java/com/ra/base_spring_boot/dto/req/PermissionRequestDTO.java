package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class PermissionRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Role IDs are required")
    private Set<Long> roleIds;
}

