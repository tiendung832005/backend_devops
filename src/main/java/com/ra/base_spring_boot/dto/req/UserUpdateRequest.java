package com.ra.base_spring_boot.dto.req;

import lombok.Data;

import java.util.Set;

@Data
public class UserUpdateRequest {
    private String fullName;
    private String username;
    private String bio;
    private Set<String> roles;
}