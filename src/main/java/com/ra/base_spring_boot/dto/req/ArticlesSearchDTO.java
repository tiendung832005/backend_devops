package com.ra.base_spring_boot.dto.req;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticlesSearchDTO {
    private String keyword; // Search by title
    private String tags; // Search by tags
    private LocalDateTime startDate; // Search by date range - start
    private LocalDateTime endDate; // Search by date range - end
    private Long authorId; // Search by author
    private Long categoryId; // Search by category
    private String status; // Filter by status
    private String type; // Filter by type
}
