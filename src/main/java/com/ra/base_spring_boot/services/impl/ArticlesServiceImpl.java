package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.ArticlesRequestDTO;
import com.ra.base_spring_boot.dto.req.ArticlesSearchDTO;
import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.dto.resp.CategoryWithHighlightDTO;
import com.ra.base_spring_boot.exception.HttpConflict;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.constants.ArticlesStatus;
import com.ra.base_spring_boot.model.constants.ArticlesType;
import com.ra.base_spring_boot.model.entity.Articles;
import com.ra.base_spring_boot.model.entity.Categories;
import com.ra.base_spring_boot.model.entity.Notifications;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.IArticlesRepository;
import com.ra.base_spring_boot.repository.ICategoriesRepository;
import com.ra.base_spring_boot.repository.INotificationRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IArticlesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticlesServiceImpl implements IArticlesService {
    private final IArticlesRepository articlesRepository;
    private final IUserRepository userRepository;
    private final ICategoriesRepository categoriesRepository;
    private final EmailService emailService;
    private final INotificationRepository notificationRepository;

    @Override
    public Page<ArticlesResponseDTO> findAll(ArticlesSearchDTO searchDTO, Pageable pageable) {
        Specification<Articles> spec = (root, query, cb) -> {
            // Add JOINs for sorting by nested properties
            if (query != null && Long.class != query.getResultType()) {
                root.fetch("author", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("category", jakarta.persistence.criteria.JoinType.LEFT);
            }

            Predicate predicate = cb.conjunction();

            if (searchDTO != null) {
                // Search by keyword (title)
                if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
                    String keyword = "%" + searchDTO.getKeyword().toLowerCase() + "%";
                    Predicate keywordPredicate = cb.like(cb.lower(root.get("title")), keyword);
                    predicate = cb.and(predicate, keywordPredicate);
                }

                // Search by tags
                if (searchDTO.getTags() != null && !searchDTO.getTags().isEmpty()) {
                    String tags = "%" + searchDTO.getTags().toLowerCase() + "%";
                    Predicate tagsPredicate = cb.like(cb.lower(root.get("tags")), tags);
                    predicate = cb.and(predicate, tagsPredicate);
                }

                // Search by date range
                if (searchDTO.getStartDate() != null) {
                    predicate = cb.and(predicate,
                            cb.greaterThanOrEqualTo(root.get("publishedAt"), searchDTO.getStartDate()));
                }
                if (searchDTO.getEndDate() != null) {
                    predicate = cb.and(predicate,
                            cb.lessThanOrEqualTo(root.get("publishedAt"), searchDTO.getEndDate()));
                }

                // Search by author
                if (searchDTO.getAuthorId() != null) {
                    predicate = cb.and(predicate, cb.equal(root.get("author").get("id"), searchDTO.getAuthorId()));
                }

                // Search by category
                if (searchDTO.getCategoryId() != null) {
                    predicate = cb.and(predicate, cb.equal(root.get("category").get("id"), searchDTO.getCategoryId()));
                }

                // Filter by status
                if (searchDTO.getStatus() != null && !searchDTO.getStatus().isEmpty()) {
                    try {
                        ArticlesStatus status = ArticlesStatus.valueOf(searchDTO.getStatus().toUpperCase());
                        predicate = cb.and(predicate, cb.equal(root.get("status"), status));
                    } catch (IllegalArgumentException e) {
                        // Ignore invalid status
                    }
                }

                // Filter by type
                if (searchDTO.getType() != null && !searchDTO.getType().isEmpty()) {
                    try {
                        ArticlesType type = ArticlesType.valueOf(searchDTO.getType().toUpperCase());
                        predicate = cb.and(predicate, cb.equal(root.get("type"), type));
                    } catch (IllegalArgumentException e) {
                        // Ignore invalid type
                    }
                }
            }

            return predicate;
        };

        return articlesRepository.findAll(spec, pageable)
                .map(ArticlesResponseDTO::new);
    }

    @Override
    public ArticlesResponseDTO findById(Long id) {
        Articles article = articlesRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Article not found with id: " + id));
        return new ArticlesResponseDTO(article);
    }

    @Override
    @Transactional
    public ArticlesResponseDTO create(ArticlesRequestDTO articlesRequestDTO) {
        // Check if slug already exists (if provided)
        if (articlesRequestDTO.getSlug() != null && !articlesRequestDTO.getSlug().isEmpty()) {
            articlesRepository.findAll().stream()
                    .filter(a -> articlesRequestDTO.getSlug().equals(a.getSlug()))
                    .findFirst()
                    .ifPresent(a -> {
                        throw new HttpConflict("Slug already exists");
                    });
        }

        // Get author
        User author = userRepository.findById(articlesRequestDTO.getAuthorId())
                .orElseThrow(() -> new HttpNotFound("Author not found with id: " + articlesRequestDTO.getAuthorId()));

        // Get category if provided
        Categories category = null;
        if (articlesRequestDTO.getCategoryId() != null) {
            category = categoriesRepository.findById(articlesRequestDTO.getCategoryId())
                    .orElseThrow(() -> new HttpNotFound(
                            "Category not found with id: " + articlesRequestDTO.getCategoryId()));
        }

        // Parse status
        ArticlesStatus status = ArticlesStatus.PENDING;
        if (articlesRequestDTO.getStatus() != null && !articlesRequestDTO.getStatus().isEmpty()) {
            try {
                status = ArticlesStatus.valueOf(articlesRequestDTO.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                status = ArticlesStatus.PENDING;
            }
        }

        // Parse type
        ArticlesType type = ArticlesType.ARTICLE;
        if (articlesRequestDTO.getType() != null && !articlesRequestDTO.getType().isEmpty()) {
            try {
                type = ArticlesType.valueOf(articlesRequestDTO.getType().toUpperCase());
            } catch (IllegalArgumentException e) {
                type = ArticlesType.ARTICLE;
            }
        }

        // Generate slug if not provided
        String slug = articlesRequestDTO.getSlug();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug(articlesRequestDTO.getTitle());
        }

        // Set highlight level
        Integer highlightLevel = articlesRequestDTO.getHighlightLevel() != null
                ? articlesRequestDTO.getHighlightLevel()
                : 0;

        // Create article
        Articles article = new Articles();
        article.setTitle(articlesRequestDTO.getTitle());
        article.setSlug(slug);
        article.setThumbnailUrl(articlesRequestDTO.getThumbnailUrl());
        article.setContent(articlesRequestDTO.getContent());
        article.setAuthor(author);
        article.setCategory(category);
        article.setStatus(status);
        article.setType(type);
        article.setTags(articlesRequestDTO.getTags());
        article.setViewCount(0);
        article.setHighlightLevel(highlightLevel);

        if (status == ArticlesStatus.APPROVED) {
            article.setPublishedAt(LocalDateTime.now());
        }

        article = articlesRepository.save(article);

        return new ArticlesResponseDTO(article);
    }

    @Override
    @Transactional
    public ArticlesResponseDTO update(Long id, ArticlesRequestDTO articlesRequestDTO) {
        Articles article = articlesRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Article not found with id: " + id));

        if (article.getStatus() == ArticlesStatus.LOCK || article.getStatus() == ArticlesStatus.REJECTED) {
            throw new HttpConflict("Không thể chỉnh sửa bài viết ở trạng thái LOCK hoặc REJECTED");
        }

        // Check if slug already exists (if changed)
        if (articlesRequestDTO.getSlug() != null && !articlesRequestDTO.getSlug().isEmpty()) {
            if (!articlesRequestDTO.getSlug().equals(article.getSlug())) {
                articlesRepository.findAll().stream()
                        .filter(a -> articlesRequestDTO.getSlug().equals(a.getSlug()))
                        .findFirst()
                        .ifPresent(a -> {
                            throw new HttpConflict("Slug already exists");
                        });
            }
        }

        // Get author
        User author = userRepository.findById(articlesRequestDTO.getAuthorId())
                .orElseThrow(() -> new HttpNotFound("Author not found with id: " + articlesRequestDTO.getAuthorId()));

        // Get category if provided
        Categories category = null;
        if (articlesRequestDTO.getCategoryId() != null) {
            category = categoriesRepository.findById(articlesRequestDTO.getCategoryId())
                    .orElseThrow(() -> new HttpNotFound(
                            "Category not found with id: " + articlesRequestDTO.getCategoryId()));
        }

        // Parse status
        if (articlesRequestDTO.getStatus() != null && !articlesRequestDTO.getStatus().isEmpty()) {
            try {
                ArticlesStatus status = ArticlesStatus.valueOf(articlesRequestDTO.getStatus().toUpperCase());

                // Prevent changing from APPROVED to PENDING
                if (article.getStatus() == ArticlesStatus.APPROVED && status == ArticlesStatus.PENDING) {
                    throw new HttpConflict("Không thể chuyển trạng thái từ APPROVED về PENDING");
                }

                article.setStatus(status);
                if (status == ArticlesStatus.APPROVED && article.getPublishedAt() == null) {
                    article.setPublishedAt(LocalDateTime.now());
                }
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }

        // Parse type
        if (articlesRequestDTO.getType() != null && !articlesRequestDTO.getType().isEmpty()) {
            try {
                ArticlesType type = ArticlesType.valueOf(articlesRequestDTO.getType().toUpperCase());
                article.setType(type);
            } catch (IllegalArgumentException e) {
                // Ignore invalid type
            }
        }

        // Update article
        article.setTitle(articlesRequestDTO.getTitle());
        if (articlesRequestDTO.getSlug() != null && !articlesRequestDTO.getSlug().isEmpty()) {
            article.setSlug(articlesRequestDTO.getSlug());
        }
        article.setThumbnailUrl(articlesRequestDTO.getThumbnailUrl());
        article.setContent(articlesRequestDTO.getContent());
        article.setAuthor(author);
        article.setCategory(category);
        article.setTags(articlesRequestDTO.getTags());

        if (articlesRequestDTO.getHighlightLevel() != null) {
            article.setHighlightLevel(articlesRequestDTO.getHighlightLevel());
        }

        article = articlesRepository.save(article);
        return new ArticlesResponseDTO(article);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Articles article = articlesRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Article not found with id: " + id));
        articlesRepository.delete(article);
    }

    @Override
    @Transactional
    public ArticlesResponseDTO highlight(Long id, Integer highlightLevel) {
        Articles article = articlesRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Article not found with id: " + id));

        article.setHighlightLevel(highlightLevel != null ? highlightLevel : 0);
        article = articlesRepository.save(article);

        return new ArticlesResponseDTO(article);
    }

    @Override
    @Transactional
    public ArticlesResponseDTO changeStatus(Long id, String status) {
        Articles article = articlesRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Article not found with id: " + id));

        if (article.getStatus() == ArticlesStatus.LOCK || article.getStatus() == ArticlesStatus.REJECTED) {
            throw new HttpConflict("Không thể chỉnh sửa bài viết ở trạng thái LOCK hoặc REJECTED");
        }

        try {
            ArticlesStatus newStatus = ArticlesStatus.valueOf(status.toUpperCase());

            // Prevent changing from APPROVED to PENDING
            if (article.getStatus() == ArticlesStatus.APPROVED && newStatus == ArticlesStatus.PENDING) {
                throw new HttpConflict("Không thể chuyển trạng thái từ APPROVED về PENDING");
            }

            article.setStatus(newStatus);

            if (newStatus == ArticlesStatus.APPROVED && article.getPublishedAt() == null) {
                article.setPublishedAt(LocalDateTime.now());
            }

            article = articlesRepository.save(article);

            Optional<User> authorOpt = userRepository.findByUsername(String.valueOf(article.getAuthor()));

            if (authorOpt.isPresent() && newStatus == ArticlesStatus.APPROVED) {
                User author = authorOpt.get();

                emailService.sendArticleApprovedMail(
                        author.getEmail(),
                        article.getTitle(),
                        article.getPublishedAt());
            }

        } catch (IllegalArgumentException e) {
            throw new HttpConflict("Invalid status: " + status);
        }

        return new ArticlesResponseDTO(article);
    }

    @Override
    public List<CategoryWithHighlightDTO> getCategoriesOrderedByHighlightLevel() {
        List<Object[]> results = categoriesRepository.findAllWithTotalHighlightLevel();
        return results.stream()
                .map(result -> {
                    Categories category = (Categories) result[0];
                    Long totalHighlight = (Long) result[1];
                    return new CategoryWithHighlightDTO(category, totalHighlight);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<ArticlesResponseDTO> getRelatedArticles(Long articleId, Long categoryId, Long authorId,
            Pageable pageable) {
        // Extract category and author from current article if needed
        Long finalCategoryId = categoryId;
        Long finalAuthorId = authorId;

        if (articleId != null) {
            Articles currentArticle = articlesRepository.findById(articleId)
                    .orElseThrow(() -> new HttpNotFound("Article not found with id: " + articleId));

            // Use current article's category if categoryId not provided
            if (finalCategoryId == null && currentArticle.getCategory() != null) {
                finalCategoryId = currentArticle.getCategory().getId();
            }

            // Use current article's author if authorId not provided
            if (finalAuthorId == null && currentArticle.getAuthor() != null) {
                finalAuthorId = currentArticle.getAuthor().getId();
            }
        }

        final Long finalCategoryIdForLambda = finalCategoryId;
        final Long finalAuthorIdForLambda = finalAuthorId;
        final Long finalArticleId = articleId;

        Specification<Articles> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            // Exclude current article if provided
            if (finalArticleId != null) {
                predicate = cb.and(predicate, cb.notEqual(root.get("id"), finalArticleId));
            }

            // Filter by approved status only
            predicate = cb.and(predicate, cb.equal(root.get("status"), ArticlesStatus.APPROVED));

            // Filter by category if provided
            if (finalCategoryIdForLambda != null) {
                predicate = cb.and(predicate, cb.equal(root.get("category").get("id"), finalCategoryIdForLambda));
            }

            // Filter by author if provided
            if (finalAuthorIdForLambda != null) {
                predicate = cb.and(predicate, cb.equal(root.get("author").get("id"), finalAuthorIdForLambda));
            }

            return predicate;
        };

        return articlesRepository.findAll(spec, pageable)
                .map(ArticlesResponseDTO::new);
    }

    @Override
    public Page<ArticlesResponseDTO> getArticlesByCategory(Long categoryId, Pageable pageable) {
        // Verify category exists
        categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new HttpNotFound("Category not found with id: " + categoryId));

        Specification<Articles> spec = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("category").get("id"), categoryId);
            // Only show approved articles
            predicate = cb.and(predicate, cb.equal(root.get("status"), ArticlesStatus.APPROVED));
            return predicate;
        };

        return articlesRepository.findAll(spec, pageable)
                .map(ArticlesResponseDTO::new);
    }

    @Override
    @Transactional
    public ArticlesResponseDTO approveArticle(Long id) {
        Articles article = articlesRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Article not found with id: " + id));

        article.setStatus(ArticlesStatus.APPROVED);
        if (article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }

        article = articlesRepository.save(article);
        return new ArticlesResponseDTO(article);
    }

    @Override
    public int countArticlesPending() {
        return articlesRepository.countArticlesByStatus(ArticlesStatus.PENDING);
    }

    private String generateSlug(String title) {
        if (title == null || title.isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim() + "-" + System.currentTimeMillis();
    }
}
