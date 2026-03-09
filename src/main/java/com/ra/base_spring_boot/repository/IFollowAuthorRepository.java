package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.entity.FollowAuthors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IFollowAuthorRepository extends JpaRepository<FollowAuthors, Long> {
    
    Optional<FollowAuthors> findByAuthor_IdAndFollower_Id(Long authorId, Long followerId);
    
    boolean existsByAuthor_IdAndFollower_Id(Long authorId, Long followerId);
    
    @Query("SELECT fa.author FROM FollowAuthors fa WHERE fa.follower.id = :followerId")
    List<com.ra.base_spring_boot.model.entity.User> findFollowedAuthorsByFollowerId(@Param("followerId") Long followerId);
}

