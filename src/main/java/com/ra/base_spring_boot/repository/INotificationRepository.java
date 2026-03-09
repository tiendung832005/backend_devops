package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.dto.req.NotificationSummaryDTO;
import com.ra.base_spring_boot.model.constants.TypeNotification;
import com.ra.base_spring_boot.model.entity.Notifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface INotificationRepository extends JpaRepository<Notifications, Long> {
    // ---------- LIST ----------
        @Query("""
        SELECT n FROM Notifications n
        JOIN FETCH n.sender
        WHERE n.receiver IS NULL
          AND n.isDeleted = false
        ORDER BY n.createdAt DESC
    """)
    Page<Notifications> findAllForAdmin(Pageable pageable);


    @Query("""
    SELECT n FROM Notifications n
    JOIN FETCH n.sender
    WHERE n.type = :type
      AND n.receiver IS NULL
      AND n.isDeleted = false
    ORDER BY n.createdAt DESC
""")
    Page<Notifications> findByTypeForAdmin(
            @Param("type") TypeNotification type,
            Pageable pageable
    );


    // ---------- COUNT ----------
    @Query("""
    SELECT COUNT(n)
    FROM Notifications n
    WHERE n.receiver IS NULL
      AND n.status = false
      AND n.isDeleted = false
""")
    Long countUnreadForAdmin();

    // ---------- SUMMARY ----------
    @Query("""
    SELECT new com.ra.base_spring_boot.dto.req.NotificationSummaryDTO(
        n.type,
        COUNT(n)
    )
    FROM Notifications n
    WHERE n.receiver IS NULL
      AND n.isDeleted = false
    GROUP BY n.type
""")
    List<NotificationSummaryDTO> summaryForAdmin();


    // ---------- SECURITY ----------
    Optional<Notifications> findByIdAndReceiverIsNullAndIsDeletedFalse(Long id);


    // ---------- BULK UPDATE ----------
    @Modifying
    @Query("""
    UPDATE Notifications n
    SET n.status = true
    WHERE n.receiver IS NULL
      AND n.status = false
      AND n.isDeleted = false
""")
    void markAllAsReadForAdmin();

}
