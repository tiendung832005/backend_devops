package com.ra.base_spring_boot.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendArticleApprovedMail(String to, String title, LocalDateTime publishedAt) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Bài viết của bạn đã được duyệt ");
        message.setText(
                "Bài viết \"" + title + "\" đã được duyệt và xuất bản vào "
                        + publishedAt
        );

        mailSender.send(message);
    }
}
