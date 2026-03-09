package com.ra.base_spring_boot.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "search_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user; // Null nếu là user chưa đăng nhập

    @Column(name = "search_query", nullable = false, length = 500)
    private String searchQuery;

    @Column(name = "search_type", nullable = true)
    private String searchType; // ARTICLE, USER, CATEGORY, ALL, etc.

    @Column(name = "filters", nullable = true, columnDefinition = "TEXT")
    private String filters; // JSON string chứa các filter đã áp dụng

    @Column(name = "result_count", nullable = true)
    private Integer resultCount; // Số lượng kết quả tìm được

    @Column(name = "ip_address", nullable = true)
    private String ipAddress;

    @Column(name = "user_agent", nullable = true, length = 500)
    private String userAgent;

    @Column(name = "session_id", nullable = true)
    private String sessionId;

    @Column(name = "clicked_result_id", nullable = true)
    private Long clickedResultId; // ID của kết quả được click (nếu có)

    @Column(name = "clicked_result_type", nullable = true)
    private String clickedResultType; // Loại kết quả được click (Article, User, etc.)

    @Column(name = "search_duration_ms", nullable = true)
    private Long searchDurationMs; // Thời gian tìm kiếm (milliseconds)

    @Column(name = "is_successful", nullable = true)
    private Boolean isSuccessful; // Tìm thấy kết quả hay không

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

