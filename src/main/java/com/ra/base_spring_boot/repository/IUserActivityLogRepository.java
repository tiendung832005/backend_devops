package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.entity.UserActivityLog;
import com.ra.base_spring_boot.model.constants.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IUserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    
    Page<UserActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    List<UserActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<UserActivityLog> findByActivityTypeOrderByCreatedAtDesc(ActivityType activityType);
    
    @Query("SELECT a FROM UserActivityLog a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<UserActivityLog> findRecentActivities(@Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM UserActivityLog a WHERE a.user.id = :userId AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<UserActivityLog> findRecentActivitiesByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}

