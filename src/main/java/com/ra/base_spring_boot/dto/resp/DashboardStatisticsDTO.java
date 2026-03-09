package com.ra.base_spring_boot.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsDTO {
    private Long total;
    private Long approved;
    private Long pending;
    private Long rejected;
    private Long locked;
    
    // For user statistics
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    
    // For interaction statistics
    private Long totalReactions;
    private Long totalLikes;
    private Long totalDislikes;
    private Long totalComments;
    private Long totalViews;
}

