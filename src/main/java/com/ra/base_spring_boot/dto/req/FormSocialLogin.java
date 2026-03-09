package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FormSocialLogin {
    @NotBlank(message = "Provider không được để trống")
    private String provider; // google, facebook, github, etc.
    
    @NotBlank(message = "Provider ID không được để trống")
    private String providerId;
    
    private String email;
    private String fullName;
    private String avatarUrl;
}

