package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.dto.resp.RecentArticleDTO;
import com.ra.base_spring_boot.model.entity.Articles;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IArticlesDashboardRepository extends JpaRepository<Articles, Long> {

    @Query("""
        SELECT new com.ra.base_spring_boot.dto.resp.RecentArticleDTO(
            a.title,
            u.email,
            a.status
        )
        FROM Articles a
        JOIN a.author u
        ORDER BY a.createdAt DESC
    """)
    List<RecentArticleDTO> findRecentArticles(Pageable pageable);
}
