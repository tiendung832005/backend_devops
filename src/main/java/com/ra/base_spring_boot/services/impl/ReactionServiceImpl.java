package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.ReactionRequestDTO;
import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.dto.resp.ReactionResponseDTO;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.constants.ReactionType;
import com.ra.base_spring_boot.model.entity.Articles;
import com.ra.base_spring_boot.model.entity.Reactions;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.IArticlesRepository;
import com.ra.base_spring_boot.repository.IReactionsRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements IReactionService
{
    private final IReactionsRepository reactionsRepository;
    private final IArticlesRepository articlesRepository;
    private final IUserRepository userRepository;

    @Override
    @Transactional
    public ReactionResponseDTO react(ReactionRequestDTO dto, String username)
    {
        Articles article = articlesRepository.findById(dto.getArticleId())
                .orElseThrow(() -> new HttpNotFound("Article not found with id: " + dto.getArticleId()));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));

        Reactions reaction = reactionsRepository.findByArticles_IdAndUser_Id(article.getId(), user.getId())
                .orElse(null);

        ReactionType newType = dto.getReactionType();

        if (reaction != null)
        {
            if (reaction.getReactionType() == newType)
            {
                // Toggle off
                reactionsRepository.delete(reaction);
                return buildResponse(article.getId(), null);
            }
            else
            {
                reaction.setReactionType(newType);
                reaction.setReactionTime(LocalDateTime.now());
                reactionsRepository.save(reaction);
            }
        }
        else
        {
            Reactions newReaction = new Reactions();
            newReaction.setArticles(article);
            newReaction.setUser(user);
            newReaction.setReactionType(newType);
            newReaction.setReactionTime(LocalDateTime.now());
            reactionsRepository.save(newReaction);
        }

        return buildResponse(article.getId(), newType);
    }

    @Override
    public ReactionResponseDTO getReactions(Long articleId, String username)
    {
        Articles article = articlesRepository.findById(articleId)
                .orElseThrow(() -> new HttpNotFound("Article not found with id: " + articleId));

        ReactionType current = null;
        if (username != null)
        {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null)
            {
                Reactions reaction = reactionsRepository.findByArticles_IdAndUser_Id(article.getId(), user.getId())
                        .orElse(null);
                if (reaction != null)
                {
                    current = reaction.getReactionType();
                }
            }
        }

        return buildResponse(article.getId(), current);
    }

    private ReactionResponseDTO buildResponse(Long articleId, ReactionType current)
    {
        long likes = reactionsRepository.countByArticles_IdAndReactionType(articleId, ReactionType.LIKE);
        long dislikes = reactionsRepository.countByArticles_IdAndReactionType(articleId, ReactionType.DISLIKE);
        return ReactionResponseDTO.builder()
                .articleId(articleId)
                .likes(likes)
                .dislikes(dislikes)
                .currentUserReaction(current)
                .build();
    }

    @Override
    public List<ArticlesResponseDTO> getLikedArticles(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));

        List<Reactions> likedReactions = reactionsRepository.findByUser_IdAndReactionType(user.getId(), ReactionType.LIKE);

        return likedReactions.stream()
                .map(reaction -> new ArticlesResponseDTO(reaction.getArticles()))
                .collect(Collectors.toList());
    }
}

