package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.entity.Articles;
import com.ra.base_spring_boot.model.entity.BookmarkArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IBookmarkArticleRepository extends JpaRepository<BookmarkArticle, Long> {

    boolean existsByArticle_IdAndUser_Id(Long articleId, Long userId);

    Optional<BookmarkArticle> findByArticle_IdAndUser_Id(Long articleId, Long userId);

    @Query("SELECT b.article FROM BookmarkArticle b WHERE b.user.id = :userId")
    Page<Articles> findBookmarkedArticles(@Param("userId") Long userId, Pageable pageable);
}
