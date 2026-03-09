package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPublicArticlesService {
    Page<ArticlesResponseDTO> findAllApprovedArticles(Pageable pageable);
    Page<ArticlesResponseDTO> findApprovedArticlesByCategory(Long categoryId, Pageable pageable);
    ArticlesResponseDTO findApprovedArticleById(Long id);
}

