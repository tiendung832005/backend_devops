package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IArticleFollowService {
    ArticlesResponseDTO followArticle(Long articleId, String username);
    void unfollowArticle(Long articleId, String username);
    Page<ArticlesResponseDTO> getFollowedArticles(String username, Pageable pageable);
}

