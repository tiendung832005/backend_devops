// src/main/java/com/ra/base_spring_boot/service/OAuth2Service.java
package com.ra.base_spring_boot.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.model.constants.StatusUser;
import com.ra.base_spring_boot.model.entity.Roles;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.IRoleRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.security.jwt.JwtProvider;
import com.ra.base_spring_boot.services.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {

    private final RestTemplate rest;
    private final ObjectMapper mapper;
    private final IUserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final IRoleRepository roleRepository;

    @Value("${google.client.id}")
    private String googleClientId;
    @Value("${google.client.secret}")
    private String googleClientSecret;

    @Value("${facebook.client.id}")
    private String fbClientId;
    @Value("${facebook.client.secret}")
    private String fbClientSecret;

    // ================= GOOGLE LOGIN ====================
    @Override
    public String loginWithGoogle(String code, String redirectUri) {

        // 1. Exchange code → access_token
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", googleClientId);
        params.put("client_secret", googleClientSecret);
        params.put("redirect_uri", redirectUri);
        params.put("grant_type", "authorization_code");

        Map<String, Object> tokenResponse =
                rest.postForObject("https://oauth2.googleapis.com/token", params, Map.class);

        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new RuntimeException("Không lấy được access_token từ Google");
        }

        String accessToken = (String) tokenResponse.get("access_token");

        // 2. Get user info
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> userInfoResponse =
                rest.exchange(
                        "https://www.googleapis.com/oauth2/v3/userinfo",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Map.class
                );

        Map<String, Object> userInfo = userInfoResponse.getBody();
        if (userInfo == null) {
            throw new RuntimeException("Không lấy được user info từ Google");
        }

        String email = (String) userInfo.get("email");
        String sub = (String) userInfo.get("sub");

        if (email == null) {
            throw new RuntimeException("Google account không trả về email");
        }

        // 3. Find or create user (ĐÂY LÀ CỐT LÕI)
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(email); // rất quan trọng
                    newUser.setFullName((String) userInfo.get("name"));
                    newUser.setAvatarUrl((String) userInfo.get("picture"));

                    newUser.setProvider("google");
                    newUser.setProviderId(sub);
                    newUser.setStatus(StatusUser.ACTIVE);

                    Roles roleReader = roleRepository.findByRoleName(RoleName.ROLE_READER)
                            .orElseThrow(() -> new RuntimeException("ROLE_READER not found"));

                    newUser.getRoles().add(roleReader);

                    return userRepository.save(newUser);
                });


        // 4. Generate JWT
        return jwtProvider.generateToken(user);
    }


    // ================= FACEBOOK LOGIN ====================
    @Override
    public String loginWithFacebook(String code, String redirectUri) {

        RestTemplate restTemplate = new RestTemplate();

        // 1. Exchange code → access_token
        String tokenUrl =
                "https://graph.facebook.com/v20.0/oauth/access_token"
                        + "?client_id=" + fbClientId
                        + "&client_secret=" + fbClientSecret
                        + "&redirect_uri=" + redirectUri
                        + "&code=" + code;

        Map<String, Object> tokenResponse =
                restTemplate.getForObject(tokenUrl, Map.class);

        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new RuntimeException("Không lấy được access_token từ Facebook");
        }

        String accessToken = (String) tokenResponse.get("access_token");

        // 2. Get user info
        String userInfoUrl =
                "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=" + accessToken;

        Map<String, Object> userInfo =
                restTemplate.getForObject(userInfoUrl, Map.class);

        if (userInfo == null || !userInfo.containsKey("id")) {
            throw new RuntimeException("Không lấy được user info từ Facebook");
        }

        String facebookId = (String) userInfo.get("id");
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");

        // Facebook có thể không trả email
        if (email == null) {
            email = "fb_" + facebookId + "@facebook.com";
        }

        String finalEmail = email;

        // 3. Find or create user
        User user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> {

                    User newUser = new User();
                    newUser.setEmail(finalEmail);
                    newUser.setUsername("fb_" + facebookId); // BẮT BUỘC
                    newUser.setFullName(name);
                    newUser.setAvatarUrl(
                            userInfo.containsKey("picture")
                                    ? ((Map<?, ?>) ((Map<?, ?>) userInfo.get("picture")).get("data")).get("url").toString()
                                    : null
                    );

                    newUser.setProvider("facebook");
                    newUser.setProviderId(facebookId);
                    newUser.setStatus(StatusUser.ACTIVE);

                    // GÁN ROLE MẶC ĐỊNH
                    Roles roleReader = roleRepository.findByRoleName(RoleName.ROLE_READER)
                            .orElseThrow(() -> new RuntimeException("ROLE_READER not found"));

                    newUser.getRoles().add(roleReader);

                    return userRepository.save(newUser);
                });

        // 4. Check trạng thái
        if (user.getStatus() == StatusUser.DELETED) {
            throw new RuntimeException("Your account is deleted");
        }

        // 5. Generate JWT
        return jwtProvider.generateToken(user);
    }

}
