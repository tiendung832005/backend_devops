package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.model.entity.Notifications;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyAdmins(Notifications notification) {
        messagingTemplate.convertAndSend(
                "/topic/admin/notifications",
                notification
        );
    }
}

