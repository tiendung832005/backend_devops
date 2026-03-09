package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.ArticlesRequestDTO;
import com.ra.base_spring_boot.dto.req.ArticlesSearchDTO;
import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.dto.resp.CategoryWithHighlightDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IArticlesService {
    Page<ArticlesResponseDTO> findAll(ArticlesSearchDTO searchDTO, Pageable pageable);
    ArticlesResponseDTO findById(Long id);
    ArticlesResponseDTO create(ArticlesRequestDTO articlesRequestDTO);
    ArticlesResponseDTO update(Long id, ArticlesRequestDTO articlesRequestDTO);
    void delete(Long id);
    ArticlesResponseDTO highlight(Long id, Integer highlightLevel);
    ArticlesResponseDTO changeStatus(Long id, String status);
    List<CategoryWithHighlightDTO> getCategoriesOrderedByHighlightLevel();
    Page<ArticlesResponseDTO> getRelatedArticles(Long articleId, Long categoryId, Long authorId, Pageable pageable);
    Page<ArticlesResponseDTO> getArticlesByCategory(Long categoryId, Pageable pageable);
    ArticlesResponseDTO approveArticle(Long id);
    int countArticlesPending();
}

