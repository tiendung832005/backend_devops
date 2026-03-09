package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.AuthResponse;

public interface OAuth2Service {
    // ================= GOOGLE LOGIN ====================
    String loginWithGoogle(String code, String redirectUri);
    // ================= FACEBOOK LOGIN ====================
    String loginWithFacebook(String code, String redirectUri);
}
