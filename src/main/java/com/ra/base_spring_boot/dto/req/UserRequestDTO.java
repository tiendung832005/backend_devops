package com.ra.base_spring_boot.dto.req;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String fullName;
    private String email;
    private String passwordHash;
    private String avatarUrl;
    private String bio;
    private Long roleId;
    private String status;
}
