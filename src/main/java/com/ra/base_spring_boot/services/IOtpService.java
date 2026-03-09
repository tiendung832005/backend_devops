package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.VerifyOtpRequest;

public interface IOtpService {
    void sendOTP(String email);
    String verifyOtp(VerifyOtpRequest verifyOtpRequest);
}
