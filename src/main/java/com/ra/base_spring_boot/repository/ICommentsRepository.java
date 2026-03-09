package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.entity.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ICommentsRepository extends JpaRepository<Comments, Long>
{
    List<Comments> findByArticle_IdAndParentIsNullAndIsHiddenFalseOrderByCreatedAtAsc(Long articleId);
    List<Comments> findByParent_IdAndIsHiddenFalseOrderByCreatedAtAsc(Long parentId);
    List<Comments> findByUser_Id(Long userId);

    @Query("SELECT c FROM Comments c WHERE c.isHidden = false " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:articleId IS NULL OR c.article.id = :articleId) " +
            "AND (:userId IS NULL OR c.user.id = :userId)")
    Page<Comments> search(@Param("keyword") String keyword,
                          @Param("articleId") Long articleId,
                          @Param("userId") Long userId,
                          Pageable pageable);

    @Query("SELECT c FROM Comments c WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.article.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:articleId IS NULL OR c.article.id = :articleId) " +
            "AND (:userId IS NULL OR c.user.id = :userId) " +
            "AND (:startDate IS NULL OR c.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR c.createdAt <= :endDate)")
    Page<Comments> advancedFilter(@Param("keyword") String keyword,
                                  @Param("articleId") Long articleId,
                                  @Param("userId") Long userId,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  Pageable pageable);

    @Query("""
        SELECT COUNT(c)
        FROM Comments c
        WHERE c.user.id = :userId
        AND c.createdAt >= :time
    """)
    long countRecentByUser(@Param("userId") Long userId,
                           @Param("time") LocalDateTime time);

    boolean existsByContent(String content);

    @Query("""
    SELECT COUNT(c)
    FROM Comments c
    WHERE c.user.id = :userId
    AND c.content = :content
    AND c.createdAt >= :since
""")
    long countSameContentByUser(@Param("userId") Long userId,
                                @Param("content") String content,
                                @Param("since") LocalDateTime since);
}

