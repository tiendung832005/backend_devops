package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.resp.DashboardStatisticsDTO;
import com.ra.base_spring_boot.services.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final IDashboardService dashboardService;

    /**
     * @apiNote Get article statistics
     */
    @GetMapping("/articles")
    public ResponseEntity<?> getArticleStatistics() {
        DashboardStatisticsDTO statistics = dashboardService.getArticleStatistics();
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(statistics)
                        .build());
    }

    /**
     * @apiNote Get recent articles
     */
    @GetMapping("/recent-articles")
    public ResponseEntity<?> getRecentArticles() {
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(dashboardService.getRecentArticles())
                        .build());
    }

    /**
     * @apiNote Get user statistics
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUserStatistics() {
        DashboardStatisticsDTO statistics = dashboardService.getUserStatistics();
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(statistics)
                        .build());
    }

    /**
     * @apiNote Get interaction statistics
     */
    @GetMapping("/interactions")
    public ResponseEntity<?> getInteractionStatistics() {
        DashboardStatisticsDTO statistics = dashboardService.getInteractionStatistics();
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(statistics)
                        .build());
    }

    /**
     * @apiNote Get category view statistics
     */
    @GetMapping("/category-views")
    public ResponseEntity<?> getCategoryViews() {
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(dashboardService.getCategoryViews())
                        .build());
    }
}
