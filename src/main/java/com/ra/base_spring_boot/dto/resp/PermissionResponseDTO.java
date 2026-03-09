package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.entity.User;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class PermissionResponseDTO {
    private Long userId;
    private String username;
    private String fullName;
    private Set<String> roles;
    private Set<Long> roleIds;

    public PermissionResponseDTO(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        if (user.getRoles() != null) {
            this.roles = user.getRoles().stream()
                    .filter(role -> role.getRoleName() != null)
                    .map(role -> role.getRoleName().name())
                    .collect(Collectors.toSet());
            this.roleIds = user.getRoles().stream()
                    .map(role -> role.getId())
                    .collect(Collectors.toSet());
        }
    }
}

