package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.resp.CategoryViewDTO;
import com.ra.base_spring_boot.dto.resp.DashboardStatisticsDTO;
import com.ra.base_spring_boot.dto.resp.RecentArticleDTO;
import com.ra.base_spring_boot.model.constants.ArticlesStatus;
import com.ra.base_spring_boot.model.constants.ReactionType;
import com.ra.base_spring_boot.model.constants.StatusUser;
import com.ra.base_spring_boot.model.entity.Articles;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.*;
import com.ra.base_spring_boot.services.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {
        private final IArticlesRepository articlesRepository;
        private final IUserRepository userRepository;
        private final IReactionsRepository reactionsRepository;
        private final ICommentsRepository commentsRepository;
        private final IArticlesDashboardRepository articlesDashboardRepository;
        private final ICategoriesRepository categoriesRepository;

        @Override
        public DashboardStatisticsDTO getArticleStatistics() {
                long total = articlesRepository.count();

                // Count by status using Specification
                long approved = articlesRepository.count(createStatusSpecification(ArticlesStatus.APPROVED));
                long pending = articlesRepository.count(createStatusSpecification(ArticlesStatus.PENDING));
                long rejected = articlesRepository.count(createStatusSpecification(ArticlesStatus.REJECTED));
                long locked = articlesRepository.count(createStatusSpecification(ArticlesStatus.LOCK));

                return DashboardStatisticsDTO.builder()
                                .total(total)
                                .approved(approved)
                                .pending(pending)
                                .rejected(rejected)
                                .locked(locked)
                                .build();
        }

        @Override
        public List<RecentArticleDTO> getRecentArticles() {
                return articlesDashboardRepository.findRecentArticles(
                                PageRequest.of(0, 5));
        }

        @Override
        public DashboardStatisticsDTO getUserStatistics() {
                long totalUsers = userRepository.count();

                // Count active users using Specification
                Specification<User> activeSpec = (root, query, cb) -> cb.equal(root.get("status"), StatusUser.ACTIVE);

                long activeUsers = userRepository.count(
                                (root, query, cb) -> cb.equal(root.get("status"), StatusUser.ACTIVE));

                long inactiveUsers = totalUsers - activeUsers;

                return DashboardStatisticsDTO.builder()
                                .totalUsers(totalUsers)
                                .activeUsers(activeUsers)
                                .inactiveUsers(inactiveUsers)
                                .build();
        }

        @Override
        public DashboardStatisticsDTO getInteractionStatistics() {
                long totalReactions = reactionsRepository.count();
                long totalComments = commentsRepository.count();

                // Count all likes and dislikes
                long totalLikes = reactionsRepository.findAll().stream()
                                .filter(r -> r.getReactionType() == ReactionType.LIKE)
                                .count();
                long totalDislikes = reactionsRepository.findAll().stream()
                                .filter(r -> r.getReactionType() == ReactionType.DISLIKE)
                                .count();

                // Calculate total views from articles - using stream for sum
                long totalViews = articlesRepository.findAll().stream()
                                .mapToLong(a -> a.getViewCount() != null ? a.getViewCount() : 0L)
                                .sum();

                return DashboardStatisticsDTO.builder()
                                .totalReactions(totalReactions)
                                .totalLikes(totalLikes)
                                .totalDislikes(totalDislikes)
                                .totalComments(totalComments)
                                .totalViews(totalViews)
                                .build();
        }

        @Override
        public List<CategoryViewDTO> getCategoryViews() {
                return categoriesRepository.getCategoryViews();
        }

        private Specification<Articles> createStatusSpecification(ArticlesStatus status) {
                return (root, query, cb) -> cb.equal(root.get("status"), status);
        }
}
