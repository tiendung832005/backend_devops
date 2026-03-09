package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.FormLogin;
import com.ra.base_spring_boot.dto.req.FormRegister;
import com.ra.base_spring_boot.dto.req.ResetPasswordRequest;
import com.ra.base_spring_boot.dto.resp.JwtResponse;
import com.ra.base_spring_boot.dto.resp.LoginResponse;

public interface IAuthService {
    void register(FormRegister formRegister);
    LoginResponse login(FormLogin formLogin);
    void logout(Long userId);
    void resetPassword(ResetPasswordRequest resetPasswordRequest);
}
