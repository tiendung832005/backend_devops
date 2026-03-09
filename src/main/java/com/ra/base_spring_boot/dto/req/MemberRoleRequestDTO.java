package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class MemberRoleRequestDTO {
    @NotEmpty(message = "At least one role is required")
    private Set<Long> roleIds;
}

