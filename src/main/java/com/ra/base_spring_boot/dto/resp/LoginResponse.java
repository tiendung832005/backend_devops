package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.StatusUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String accessToken;

    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;

    private StatusUser status;
    private String provider;

    private Set<String> roles;
}

