package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.entity.CommentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICommentReportRepository extends JpaRepository<CommentReport, Long> {
    
    Optional<CommentReport> findByComment_IdAndReporter_Id(Long commentId, Long reporterId);
    
    boolean existsByComment_IdAndReporter_Id(Long commentId, Long reporterId);
    
    @Query("SELECT COUNT(DISTINCT cr.reporter.id) FROM CommentReport cr WHERE cr.comment.id = :commentId")
    Long countDistinctReportersByCommentId(@Param("commentId") Long commentId);
}

