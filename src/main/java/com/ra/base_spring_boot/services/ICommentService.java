package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.CommentAdvancedFilterDTO;
import com.ra.base_spring_boot.dto.req.CommentRequestDTO;
import com.ra.base_spring_boot.dto.req.CommentSearchDTO;
import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.dto.resp.CommentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICommentService
{
    CommentResponseDTO create(CommentRequestDTO dto, String username);
    List<CommentResponseDTO> getByArticle(Long articleId);
    List<ArticlesResponseDTO> getCommentedArticles(String username);
    Page<CommentResponseDTO> search(CommentSearchDTO dto, Pageable pageable);
    Page<CommentResponseDTO> advancedFilter(CommentAdvancedFilterDTO dto, String username, Pageable pageable);
    void delete(Long id);
    CommentResponseDTO toggleHidden(Long id, String reason, String username);
}


