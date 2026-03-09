package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.ArticlesRequestDTO;
import com.ra.base_spring_boot.dto.req.ArticlesSearchDTO;
import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.dto.resp.CategoryWithHighlightDTO;
import com.ra.base_spring_boot.repository.IArticleFollowRepository;
import com.ra.base_spring_boot.services.IArticleFollowService;
import com.ra.base_spring_boot.services.IArticlesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/articles")
@RequiredArgsConstructor
public class ArticlesController {
        private final IArticlesService articlesService;
        private final IArticleFollowService articleFollowService;

        /**
         * Helper method to map frontend sort field names to entity field names
         */
        private String mapSortField(String sortBy) {
                if (sortBy == null)
                        return "highlightLevel";
                return switch (sortBy) {
                        case "authorName" -> "author.fullName";
                        case "categoryName" -> "category.name";
                        default -> sortBy;
                };
        }

        /**
         * @apiNote Get all articles with pagination and search
         */
        @GetMapping
        public ResponseEntity<?> getAllArticles(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String tags,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                        @RequestParam(required = false) Long authorId,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String type,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "highlightLevel") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {
                ArticlesSearchDTO searchDTO = new ArticlesSearchDTO();
                searchDTO.setKeyword(keyword);
                searchDTO.setTags(tags);
                searchDTO.setStartDate(startDate);
                searchDTO.setEndDate(endDate);
                searchDTO.setAuthorId(authorId);
                searchDTO.setCategoryId(categoryId);
                searchDTO.setStatus(status);
                searchDTO.setType(type);

                String mappedSortBy = mapSortField(sortBy);
                Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(mappedSortBy).ascending()
                                : Sort.by(mappedSortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<ArticlesResponseDTO> articles = articlesService.findAll(searchDTO, pageable);

                return ResponseEntity.ok().body(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(articles)
                                                .build());
        }

        /**
         * @apiNote Get article by id
         */
        @GetMapping("/{id}")
        public ResponseEntity<?> getArticleById(@PathVariable Long id) {
                ArticlesResponseDTO article = articlesService.findById(id);
                return ResponseEntity.ok().body(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(article)
                                                .build());
        }

        /**
         * @apiNote Create new article
         */
        @PostMapping
        public ResponseEntity<?> createArticle(@Valid @RequestBody ArticlesRequestDTO articlesRequestDTO) {
                ArticlesResponseDTO article = articlesService.create(articlesRequestDTO);
                return ResponseEntity.created(URI.create("/api/v1/admin/articles/" + article.getId())).body(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.CREATED)
                                                .code(201)
                                                .data(article)
                                                .build());
        }

        /**
         * @apiNote Update article
         */
        @PutMapping("/{id}")
        public ResponseEntity<?> updateArticle(@PathVariable Long id,
                        @Valid @RequestBody ArticlesRequestDTO articlesRequestDTO) {
                ArticlesResponseDTO article = articlesService.update(id, articlesRequestDTO);
                return ResponseEntity.ok().body(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(article)
                                                .build());
        }

        /**
         * @apiNote Delete article
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<?> deleteArticle(@PathVariable Long id) {
                articlesService.delete(id);
                return ResponseEntity.ok().body(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data("Article deleted successfully")
                                                .build());
        }

        /**
         * @apiNote Highlight article by setting highlightLevel
         */
        @PutMapping("/{id}/highlight")
        public ResponseEntity<?> highlightArticle(
                        @PathVariable Long id,
                        @RequestParam Integer highlightLevel) {
                ArticlesResponseDTO article = articlesService.highlight(id, highlightLevel);
                return ResponseEntity.ok().body(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(article)
                                                .build());
        }

        /**
         * @apiNote Change article status (hide/show) - PENDING, APPROVED, REJECTED,
         *          LOCK
         */
        @PutMapping("/{id}/status")
        public ResponseEntity<?> changeArticleStatus(
                        @PathVariable Long id,
                        @RequestParam String status) {
                ArticlesResponseDTO article = articlesService.changeStatus(id, status);
                return ResponseEntity.ok().body(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(article)
                                                .build());
        }

        /**
         * @apiNote Get categories ordered by total highlightLevel (sum of all articles'
         *          highlightLevel in that category)
         */
        @GetMapping("/categories/by-highlight")
        public ResponseEntity<?> getCategoriesOrderedByHighlightLevel() {
                List<CategoryWithHighlightDTO> categories = articlesService.getCategoriesOrderedByHighlightLevel();
                return ResponseEntity.ok().body(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(categories)
                                                .build());
        }

        /**
         * @apiNote Get related articles (filter by category and/or author)
         * @param articleId  Optional: current article ID to exclude and use its
         *                   category/author
         * @param categoryId Optional: filter by category ID
         * @param authorId   Optional: filter by author ID
         */
        @GetMapping("/related")
        public ResponseEntity<?> getRelatedArticles(
                        @RequestParam(required = false) Long articleId,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) Long authorId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "publishedAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {
                Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<ArticlesResponseDTO> articles = articlesService.getRelatedArticles(articleId, categoryId, authorId,
                                pageable);

                return ResponseEntity.ok().body(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(articles)
                                                .build());
        }

        /**
         * @apiNote Get articles by category (only approved articles)
         */
        @GetMapping("/by-category/{categoryId}")
        public ResponseEntity<?> getArticlesByCategory(
                        @PathVariable Long categoryId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "publishedAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {
                Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<ArticlesResponseDTO> articles = articlesService.getArticlesByCategory(categoryId, pageable);

                return ResponseEntity.ok().body(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(articles)
                                                .build());
        }

        /**
         * @apiNote Admin approve article (set status to APPROVED)
         */
        @PutMapping("/{id}/approve")
        public ResponseEntity<?> approveArticle(@PathVariable Long id) {
                ArticlesResponseDTO article = articlesService.approveArticle(id);
                return ResponseEntity.ok().body(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(article)
                                                .build());
        }

        @GetMapping("/countPending")
        public ResponseEntity<?> countArticlesPending() {
                return ResponseEntity.ok().body(
                                ResponseWrapper.builder().status(HttpStatus.OK)
                                                .code(200)
                                                .data(articlesService.countArticlesPending())
                                                .build());
        }
}
