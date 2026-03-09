package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.FormLogin;
import com.ra.base_spring_boot.dto.req.FormRegister;
import com.ra.base_spring_boot.dto.req.ResetPasswordRequest;
import com.ra.base_spring_boot.dto.resp.LoginResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpConflict;
import com.ra.base_spring_boot.model.constants.ActivityType;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.model.constants.StatusUser;
import com.ra.base_spring_boot.model.entity.Roles;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.security.jwt.JwtProvider;
import com.ra.base_spring_boot.security.principle.MyUserDetails;
import com.ra.base_spring_boot.services.IActivityLogService;
import com.ra.base_spring_boot.services.IAuthService;
import com.ra.base_spring_boot.services.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService
{
    private final IRoleService roleService;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final IActivityLogService activityLogService;

    @Override
    public void register(FormRegister formRegister){
        if (formRegister.getEmail() != null &&
                userRepository.findByEmail(formRegister.getEmail()).isPresent()) {
            throw new HttpConflict("Email already exists");
        }
        if (userRepository.findByUsername(formRegister.getUsername()).isPresent()) {
            throw new HttpConflict("Username already exists");
        }
        Set<Roles> roles = new HashSet<>();
        roles.add(roleService.findByRoleName(RoleName.ROLE_READER));
        User user = User.builder()
                .fullName(formRegister.getFullName())
                .username(formRegister.getUsername())
                .password(passwordEncoder.encode(formRegister.getPassword()))
                .status(StatusUser.ACTIVE)
                .roles(roles)
                .build();
        userRepository.save(user);
    }

    @Override
    public LoginResponse login(FormLogin formLogin) {
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            formLogin.getLogin(),
                            formLogin.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new HttpBadRequest("Username or password is incorrect");
        }

        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if (Boolean.FALSE.equals(user.getStatus())) {
            throw new HttpBadRequest("Your account is blocked");
        }

        // Log login activity
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            activityLogService.logActivity(
                    user,
                    ActivityType.LOGIN,
                    "User logged in successfully",
                    request
            );
        }

        return LoginResponse.builder()
                .accessToken(jwtProvider.generateToken(user))
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .provider(user.getProvider())
                .status(user.getStatus())
                .roles(
                        userDetails.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toSet())
                )
                .build();
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }


    @Override
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            activityLogService.logActivity(
                    user,
                    ActivityType.LOGOUT,
                    "User logged out",
                    request
            );
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String email = jwtProvider.extractEmailFromResetToken(resetPasswordRequest.getToken());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        userRepository.save(user);
    }



}