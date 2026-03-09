package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.WriterArticlesRequestDTO;
import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.services.IWriterArticlesService;
import jakarta.validation.Valid;
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

import java.net.URI;

@RestController
@RequestMapping("/api/v1/writer/articles")
@RequiredArgsConstructor
public class WriterArticlesController {
    private final IWriterArticlesService writerArticlesService;

    /**
     * @apiNote Create new article (only ROLE_WRITER)
     */
    @PostMapping
    public ResponseEntity<?> createArticle(@Valid @RequestBody WriterArticlesRequestDTO articlesRequestDTO) {
        String username = getCurrentUsername();
        ArticlesResponseDTO article = writerArticlesService.create(articlesRequestDTO, username);
        return ResponseEntity.created(URI.create("/api/v1/writer/articles/" + article.getId())).body(
                ResponseWrapper.builder()
                        .status(HttpStatus.CREATED)
                        .code(201)
                        .data(article)
                        .build()
        );
    }

    /**
     * @apiNote Update article (only ROLE_WRITER, status will be set to PENDING after update)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticle(@PathVariable Long id, @Valid @RequestBody WriterArticlesRequestDTO articlesRequestDTO) {
        String username = getCurrentUsername();
        ArticlesResponseDTO article = writerArticlesService.update(id, articlesRequestDTO, username);
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(article)
                        .build()
        );
    }

    /**
     * @apiNote Get my articles (only ROLE_WRITER)
     */
    @GetMapping
    public ResponseEntity<?> getMyArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        String username = getCurrentUsername();
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ArticlesResponseDTO> articles = writerArticlesService.getMyArticles(username, pageable);

        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(articles)
                        .build()
        );
    }

    /**
     * @apiNote Get my article by id (only ROLE_WRITER)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMyArticleById(@PathVariable Long id) {
        String username = getCurrentUsername();
        ArticlesResponseDTO article = writerArticlesService.getMyArticleById(id, username);
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(article)
                        .build()
        );
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}

