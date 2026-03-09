package com.ra.base_spring_boot.model.entity;

import com.ra.base_spring_boot.model.constants.AdminActivityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "admin_activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private AdminActivityType activityType;

    @Column(name = "action_description", nullable = true, length = 1000)
    private String actionDescription;

    @Column(name = "target_user_id", nullable = true)
    private Long targetUserId; // User bị tác động (nếu có)

    @Column(name = "target_entity_type", nullable = true)
    private String targetEntityType; // Loại entity bị tác động (Article, User, Category, etc.)

    @Column(name = "target_entity_id", nullable = true)
    private Long targetEntityId; // ID của entity bị tác động

    @Column(name = "old_value", nullable = true, columnDefinition = "TEXT")
    private String oldValue; // Giá trị cũ (JSON string hoặc text)

    @Column(name = "new_value", nullable = true, columnDefinition = "TEXT")
    private String newValue; // Giá trị mới (JSON string hoặc text)

    @Column(name = "ip_address", nullable = true)
    private String ipAddress;

    @Column(name = "user_agent", nullable = true, length = 500)
    private String userAgent;

    @Column(name = "request_url", nullable = true, length = 500)
    private String requestUrl;

    @Column(name = "request_method", nullable = true)
    private String requestMethod; // GET, POST, PUT, DELETE

    @Column(name = "status", nullable = true)
    private String status; // SUCCESS, FAILED, ERROR

    @Column(name = "error_message", nullable = true, length = 1000)
    private String errorMessage;

    @Column(name = "reason", nullable = true, length = 500)
    private String reason; // Lý do thực hiện hành động (ví dụ: lý do ban user)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

