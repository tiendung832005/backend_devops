package com.ra.base_spring_boot.dto.req;

import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    private String fullName;
    private String email;
    private String avatarUrl;
    private String bio;
}
