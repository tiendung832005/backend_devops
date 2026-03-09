package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.VerifyOtpRequest;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.security.jwt.JwtProvider;
import com.ra.base_spring_boot.services.IOtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class IOtpServiceImpl implements IOtpService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private IUserRepository  userRepository;
    @Autowired
    private JwtProvider  jwtProvider;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void sendOTP(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(()->new HttpNotFound("Email not found"));
        String otp = generateOtp();

        String key = "otp:email:" + email;
        redisTemplate.opsForValue()
                .set(key, otp, Duration.ofMinutes(5));

        sendOtpEmail(email, otp);
    }

    @Override
    public String verifyOtp(VerifyOtpRequest verifyOtpRequest) {

        String email = verifyOtpRequest.getEmail();
        String inputOtp = verifyOtpRequest.getOtp();

        String key = "otp:email:" + email;
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            throw new RuntimeException("OTP expired or not found");
        }

        if (!storedOtp.equals(inputOtp)) {
            throw new RuntimeException("Invalid OTP");
        }

        redisTemplate.delete(key);

        return jwtProvider.generateResetPasswordToken(email);
    }


    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP code is: " + otp + "\nThis OTP will expire in 5 minutes.");

        mailSender.send(message);
    }
    private String generateOtp() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}
