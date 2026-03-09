package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.NotificationSummaryDTO;
import com.ra.base_spring_boot.dto.resp.NotificationResponse;
import com.ra.base_spring_boot.model.constants.TypeNotification;
import com.ra.base_spring_boot.model.entity.Notifications;
import com.ra.base_spring_boot.repository.INotificationRepository;
import com.ra.base_spring_boot.services.INotificationService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements INotificationService {

    private final INotificationRepository notificationRepository;

    // ================= COUNT =================
    @Override
    public Long countUnreadNotifications() {
        return notificationRepository.countUnreadForAdmin();
    }

    // ================= GET ALL =================
    @Override
    public Page<NotificationResponse> getNotifications(
            Pageable pageable
    ) {

        Page<Notifications> notifications =
                notificationRepository.findAllForAdmin(pageable);

        return notifications.map(this::mapToResponse);
    }

    // ================= GET BY TYPE =================
    @Override
    public Page<NotificationResponse> getNotificationsByType(
            TypeNotification type,
            Pageable pageable
    ) {

        Page<Notifications> notifications =
                notificationRepository.findByTypeForAdmin(type, pageable);

        return notifications.map(this::mapToResponse);
    }

    // ================= SUMMARY =================
    @Override
    @Transactional(readOnly = true)
    public List<NotificationSummaryDTO> getNotificationSummary() {
        return notificationRepository.summaryForAdmin();
    }

    // ================= MARK ONE =================
    @Override
    public void markAsRead(Long notificationId) {

        Notifications notification =
                notificationRepository.findByIdAndReceiverIsNullAndIsDeletedFalse(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException("Notification not found or access denied"));

        notification.setStatus(true);
    }

    // ================= MARK ALL =================
    @Override
    public void markAllAsRead() {
        notificationRepository.markAllAsReadForAdmin();
    }

    @Override
    public void deleteNotification(Long notificationId) {
        Notifications notification =
                notificationRepository.findByIdAndReceiverIsNullAndIsDeletedFalse(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException("Notification not found or access denied"));

        notification.setIsDeleted(true);
    }

    // ================= MAPPER =================
    private NotificationResponse mapToResponse(Notifications n) {

        return new NotificationResponse(
                n.getId(),

                n.getSender().getId(),
                n.getSender().getUsername(),

                n.getReceiver() != null ? n.getReceiver().getId() : null,
                n.getReceiver() != null ? n.getReceiver().getUsername() : null,

                n.getMessage(),
                n.getActionLink(),
                n.getType(),
                n.getStatus(),
                n.getCreatedAt()
        );
    }
}
