package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.constants.ReactionType;
import com.ra.base_spring_boot.model.entity.Reactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IReactionsRepository extends JpaRepository<Reactions, Long>
{
    Optional<Reactions> findByArticles_IdAndUser_Id(Long articleId, Long userId);
    long countByArticles_IdAndReactionType(Long articleId, ReactionType reactionType);
    List<Reactions> findByUser_IdAndReactionType(Long userId, ReactionType reactionType);
}

