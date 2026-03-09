package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IBookmarkArticleService {
    ArticlesResponseDTO bookmarkArticle(Long articleId, String username);
    void removeBookmark(Long articleId, String username);
    Page<ArticlesResponseDTO> getUserBookmarks(String username, Pageable pageable);
}
