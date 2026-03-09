package com.ra.base_spring_boot.model.entity;

import com.ra.base_spring_boot.model.constants.ActivityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "user_activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Column(name = "action_description", nullable = true, length = 1000)
    private String actionDescription;

    @Column(name = "ip_address", nullable = true)
    private String ipAddress;

    @Column(name = "user_agent", nullable = true, length = 500)
    private String userAgent;

    @Column(name = "entity_type", nullable = true)
    private String entityType; // Loại entity liên quan (Article, Comment, User, etc.)

    @Column(name = "entity_id", nullable = true)
    private Long entityId; // ID của entity liên quan

    @Column(name = "request_url", nullable = true, length = 500)
    private String requestUrl;

    @Column(name = "request_method", nullable = true)
    private String requestMethod; // GET, POST, PUT, DELETE

    @Column(name = "status", nullable = true)
    private String status; // SUCCESS, FAILED, ERROR

    @Column(name = "error_message", nullable = true, length = 1000)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

