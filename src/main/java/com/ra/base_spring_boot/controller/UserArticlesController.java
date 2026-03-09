package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.services.IArticleFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/articles")
@RequiredArgsConstructor
public class UserArticlesController {
    private final IArticleFollowService articleFollowService;

    /**
     * @apiNote Follow an article (only ROLE_READER, ROLE_WRITER)
     */
    @PostMapping("/{articleId}/follow")
    public ResponseEntity<?> followArticle(@PathVariable Long articleId) {
        String username = getCurrentUsername();
        ArticlesResponseDTO article = articleFollowService.followArticle(articleId, username);
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(article)
                        .build()
        );
    }

    /**
     * @apiNote Unfollow an article
     */
    @DeleteMapping("/{articleId}/follow")
    public ResponseEntity<?> unfollowArticle(@PathVariable Long articleId) {
        String username = getCurrentUsername();
        articleFollowService.unfollowArticle(articleId, username);
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data("Article unfollowed successfully")
                        .build()
        );
    }

    /**
     * @apiNote Get all followed articles of current user
     */
    @GetMapping("/followed")
    public ResponseEntity<?> getFollowedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        String username = getCurrentUsername();
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ArticlesResponseDTO> articles = articleFollowService.getFollowedArticles(username, pageable);

        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(articles)
                        .build()
        );
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}

