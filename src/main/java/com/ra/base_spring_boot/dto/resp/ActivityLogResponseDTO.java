package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.ActivityType;
import com.ra.base_spring_boot.model.entity.UserActivityLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogResponseDTO {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private ActivityType activityType;
    private String actionDescription;
    private String ipAddress;
    private String userAgent;
    private String entityType;
    private Long entityId;
    private String requestUrl;
    private String requestMethod;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;

    public ActivityLogResponseDTO(UserActivityLog log) {
        this.id = log.getId();
        this.userId = log.getUser().getId();
        this.username = log.getUser().getUsername();
        this.fullName = log.getUser().getFullName();
        this.email = log.getUser().getEmail();
        this.activityType = log.getActivityType();
        this.actionDescription = log.getActionDescription();
        this.ipAddress = log.getIpAddress();
        this.userAgent = log.getUserAgent();
        this.entityType = log.getEntityType();
        this.entityId = log.getEntityId();
        this.requestUrl = log.getRequestUrl();
        this.requestMethod = log.getRequestMethod();
        this.status = log.getStatus();
        this.errorMessage = log.getErrorMessage();
        this.createdAt = log.getCreatedAt();
    }
}

