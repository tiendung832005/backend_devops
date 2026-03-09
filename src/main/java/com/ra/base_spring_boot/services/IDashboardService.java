package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.resp.CategoryViewDTO;
import com.ra.base_spring_boot.dto.resp.DashboardStatisticsDTO;
import com.ra.base_spring_boot.dto.resp.RecentArticleDTO;

import java.util.List;

public interface IDashboardService {
    DashboardStatisticsDTO getArticleStatistics();

    DashboardStatisticsDTO getUserStatistics();

    DashboardStatisticsDTO getInteractionStatistics();

    List<RecentArticleDTO> getRecentArticles();

    List<CategoryViewDTO> getCategoryViews();
}
