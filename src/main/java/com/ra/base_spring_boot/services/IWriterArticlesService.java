package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.WriterArticlesRequestDTO;
import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IWriterArticlesService {
    ArticlesResponseDTO create(WriterArticlesRequestDTO articlesRequestDTO, String username);
    ArticlesResponseDTO update(Long id, WriterArticlesRequestDTO articlesRequestDTO, String username);
    Page<ArticlesResponseDTO> getMyArticles(String username, Pageable pageable);
    ArticlesResponseDTO getMyArticleById(Long id, String username);
}

