package com.ra.base_spring_boot.dto.req;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentAdvancedFilterDTO {
    private Long userId;          // Lọc theo user
    private String keyword;       // Lọc theo keyword trong content
    private Long articleId;       // Lọc theo bài viết
    private LocalDateTime startDate;  // Ngày bắt đầu
    private LocalDateTime endDate;    // Ngày kết thúc
}

