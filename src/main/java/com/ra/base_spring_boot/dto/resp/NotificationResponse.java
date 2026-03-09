package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.TypeNotification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private Long receiverId;
    private String receiverUsername;
    private String message;
    private String actionLink;
    private TypeNotification type;
    private Boolean status;
    private LocalDateTime createdAt;
}
