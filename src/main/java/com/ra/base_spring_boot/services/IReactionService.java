package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.ReactionRequestDTO;
import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.dto.resp.ReactionResponseDTO;

import java.util.List;

public interface IReactionService
{
    ReactionResponseDTO react(ReactionRequestDTO dto, String username);
    ReactionResponseDTO getReactions(Long articleId, String username);
    List<ArticlesResponseDTO> getLikedArticles(String username);
}

