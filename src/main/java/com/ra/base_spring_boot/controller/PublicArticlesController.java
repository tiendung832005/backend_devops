package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.services.IPublicArticlesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class PublicArticlesController {
    private final IPublicArticlesService publicArticlesService;

    /**
     * @apiNote Get all approved articles (public access)
     */
    @GetMapping
    public ResponseEntity<?> getAllApprovedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ArticlesResponseDTO> articles = publicArticlesService.findAllApprovedArticles(pageable);

        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(articles)
                        .build()
        );
    }

    /**
     * @apiNote Get approved articles by category (public access)
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getApprovedArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ArticlesResponseDTO> articles = publicArticlesService.findApprovedArticlesByCategory(categoryId, pageable);

        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(articles)
                        .build()
        );
    }

    /**
     * @apiNote Get approved article by id (public access)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getApprovedArticleById(@PathVariable Long id) {
        ArticlesResponseDTO article = publicArticlesService.findApprovedArticleById(id);
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(article)
                        .build()
        );
    }
}

