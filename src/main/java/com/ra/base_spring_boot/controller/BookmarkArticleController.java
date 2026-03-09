package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.services.IBookmarkArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
public class BookmarkArticleController {

    private final IBookmarkArticleService bookmarkService;

    /**
     * Save bookmark
     */
    @PostMapping("/{id}")
    public ResponseEntity<?> bookmark(@PathVariable Long id, Principal principal) {

        ArticlesResponseDTO result =
                bookmarkService.bookmarkArticle(id, principal.getName());

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(result)
                        .build()
        );
    }

    /**
     * Remove bookmark
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeBookmark(@PathVariable Long id,
                                            Principal principal) {

        bookmarkService.removeBookmark(id, principal.getName());

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .build()
        );
    }

    /**
     * Get user bookmarked articles
     */
    @GetMapping
    public ResponseEntity<?> getBookmarkedArticles(Principal principal,
                                                   Pageable pageable) {

        Page<ArticlesResponseDTO> result =
                bookmarkService.getUserBookmarks(principal.getName(), pageable);

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(result)
                        .build()
        );
    }
}
