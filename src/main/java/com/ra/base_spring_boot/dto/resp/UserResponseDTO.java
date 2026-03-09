package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.StatusUser;
import com.ra.base_spring_boot.model.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class UserResponseDTO {

    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private Set<String> roles;
    private StatusUser status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.avatarUrl = user.getAvatarUrl();
        this.bio = user.getBio();
        this.status = user.getStatus();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.roles = user.getRoles()
                .stream()
                .map(r -> r.getRoleName().name())
                .collect(Collectors.toSet());
    }
}
