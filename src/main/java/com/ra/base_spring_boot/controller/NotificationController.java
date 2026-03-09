package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.NotificationSummaryDTO;
import com.ra.base_spring_boot.dto.resp.NotificationResponse;
import com.ra.base_spring_boot.model.constants.TypeNotification;
import com.ra.base_spring_boot.security.principle.MyUserDetails;
import com.ra.base_spring_boot.services.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;

    /**
     * Lấy tổng số thông báo chưa đọc (icon chuông)
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> countUnread(
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        Long total =
                notificationService.countUnreadNotifications();

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(total)
                        .build()
        );
    }

    /**
     * Lấy danh sách thông báo (có phân trang)
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(
            @AuthenticationPrincipal MyUserDetails userDetails,
            Pageable pageable
    ) {

        Page<NotificationResponse> notifications =
                notificationService.getNotifications(
                        pageable
                );

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(notifications)
                        .build()
        );
    }

    /**
     * Lấy thông báo theo loại (group drill-down)
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getByType(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable TypeNotification type,
            Pageable pageable
    ) {

        Page<NotificationResponse> notifications =
                notificationService.getNotificationsByType(
                        type,
                        pageable
                );

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(notifications)
                        .build()
        );
    }

    /**
     * Group + count (dashboard)
     */
    @GetMapping("/summary")
    public ResponseEntity<?> summary(
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {

        List<NotificationSummaryDTO> summary =
                notificationService.getNotificationSummary();

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(summary)
                        .build()
        );
    }

    /**
     * Đánh dấu 1 thông báo đã đọc
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {

        notificationService.markAsRead(
                id);

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .message("Notification marked as read")
                        .build()
        );
    }

    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {

        notificationService.markAllAsRead(
        );

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .message("All notifications marked as read")
                        .build()
        );
    }
    /**
     * Xoá một thông báo
     */
        @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {

        notificationService.deleteNotification(
                id);

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .message("Notification deleted")
                        .build()
        );
    }
}


