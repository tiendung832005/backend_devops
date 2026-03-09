package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.NotificationSummaryDTO;
import com.ra.base_spring_boot.dto.resp.NotificationResponse;
import com.ra.base_spring_boot.model.constants.TypeNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface INotificationService {

    Long countUnreadNotifications();

    Page<NotificationResponse> getNotifications( Pageable pageable);

    Page<NotificationResponse> getNotificationsByType(
            TypeNotification type,
            Pageable pageable
    );

    List<NotificationSummaryDTO> getNotificationSummary();

    void markAsRead(Long notificationId);

    void markAllAsRead();

    void deleteNotification(Long notificationId);
}
