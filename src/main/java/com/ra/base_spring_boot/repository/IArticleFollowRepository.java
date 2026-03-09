package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.constants.ArticlesStatus;
import com.ra.base_spring_boot.model.entity.ArticleFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IArticleFollowRepository extends JpaRepository<ArticleFollow, Long> {

    Optional<ArticleFollow> findByArticle_IdAndUser_Id(Long articleId, Long userId);

    boolean existsByArticle_IdAndUser_Id(Long articleId, Long userId);

    @Query("SELECT af.article FROM ArticleFollow af WHERE af.user.id = :userId AND af.article.status = :status")
    Page<com.ra.base_spring_boot.model.entity.Articles> findArticlesByUserId(
            @Param("userId") Long userId,
            @Param("status") ArticlesStatus status,
            Pageable pageable
    );
}

