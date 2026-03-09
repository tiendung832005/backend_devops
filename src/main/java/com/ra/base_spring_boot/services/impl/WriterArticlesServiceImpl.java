package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.WriterArticlesRequestDTO;
import com.ra.base_spring_boot.dto.resp.ArticlesResponseDTO;
import com.ra.base_spring_boot.exception.HttpConflict;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.constants.ArticlesStatus;
import com.ra.base_spring_boot.model.constants.ArticlesType;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.model.constants.TypeNotification;
import com.ra.base_spring_boot.model.entity.*;
import com.ra.base_spring_boot.repository.IArticlesRepository;
import com.ra.base_spring_boot.repository.ICategoriesRepository;
import com.ra.base_spring_boot.repository.INotificationRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IWriterArticlesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WriterArticlesServiceImpl implements IWriterArticlesService {
    private final IArticlesRepository articlesRepository;
    private final IUserRepository userRepository;
    private final ICategoriesRepository categoriesRepository;
    private final INotificationRepository notificationRepository;
    private final NotificationSocketService  notificationSocketService;
    @Override
    @Transactional
    public ArticlesResponseDTO create(WriterArticlesRequestDTO articlesRequestDTO, String username) {
        // Get current user
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));

        // Check if user has WRITER role
        Set<Roles> userRoles = currentUser.getRoles();
        boolean hasWriterRole = userRoles.stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_WRITER);

        if (!hasWriterRole) {
            throw new HttpForbiden("Only WRITER role can create articles");
        }

        // Check if slug already exists (if provided)
        if (articlesRequestDTO.getSlug() != null && !articlesRequestDTO.getSlug().isEmpty()) {
            articlesRepository.findAll().stream()
                    .filter(a -> articlesRequestDTO.getSlug().equals(a.getSlug()))
                    .findFirst()
                    .ifPresent(a -> {
                        throw new HttpConflict("Slug already exists");
                    });
        }

        // Get category if provided
        Categories category = null;
        if (articlesRequestDTO.getCategoryId() != null) {
            category = categoriesRepository.findById(articlesRequestDTO.getCategoryId())
                    .orElseThrow(() -> new HttpNotFound("Category not found with id: " + articlesRequestDTO.getCategoryId()));
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

        // Create article - always set status to PENDING for WRITER
        Articles article = new Articles();
        article.setTitle(articlesRequestDTO.getTitle());
        article.setSlug(slug);
        article.setThumbnailUrl(articlesRequestDTO.getThumbnailUrl());
        article.setContent(articlesRequestDTO.getContent());
        article.setAuthor(currentUser); // Use current user as author
        article.setCategory(category);
        article.setStatus(ArticlesStatus.PENDING); // Always PENDING when created by WRITER
        article.setType(type);
        article.setTags(articlesRequestDTO.getTags());
        article.setViewCount(0);
        article.setHighlightLevel(0); // WRITER cannot set highlight level

        article = articlesRepository.save(article);
        Notifications notification = createNotification(article);
        // gửi realtime
        notificationSocketService.notifyAdmins(notification);
        return new ArticlesResponseDTO(article);
    }

    @Override
    @Transactional
    public ArticlesResponseDTO update(Long id, WriterArticlesRequestDTO articlesRequestDTO, String username) {
        // Get current user
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));

        // Check if user has WRITER role
        Set<Roles> userRoles = currentUser.getRoles();
        boolean hasWriterRole = userRoles.stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_WRITER);

        if (!hasWriterRole) {
            throw new HttpForbiden("Only WRITER role can update articles");
        }

        // Get article
        Articles article = articlesRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Article not found with id: " + id));

        // Check if current user is the author
        if (!article.getAuthor().getId().equals(currentUser.getId())) {
            throw new HttpForbiden("You can only update your own articles");
        }

        // Check if article is REJECTED or LOCK - cannot be updated
        if (article.getStatus() == ArticlesStatus.REJECTED || article.getStatus() == ArticlesStatus.LOCK) {
            throw new HttpForbiden("Article cannot be changed. Status: " + article.getStatus());
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

        // Get category if provided
        Categories category = null;
        if (articlesRequestDTO.getCategoryId() != null) {
            category = categoriesRepository.findById(articlesRequestDTO.getCategoryId())
                    .orElseThrow(() -> new HttpNotFound("Category not found with id: " + articlesRequestDTO.getCategoryId()));
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
        article.setCategory(category);
        article.setTags(articlesRequestDTO.getTags());

        // IMPORTANT: When WRITER updates, set status back to PENDING
        article.setStatus(ArticlesStatus.PENDING);
        article.setPublishedAt(null); // Clear published date

        article = articlesRepository.save(article);
        return new ArticlesResponseDTO(article);
    }

    @Override
    public Page<ArticlesResponseDTO> getMyArticles(String username, Pageable pageable) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));

        // Check if user has WRITER or ADMIN role
        Set<Roles> userRoles = currentUser.getRoles();
        boolean hasWriterRole = userRoles.stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_WRITER);
        boolean hasAdminRole = userRoles.stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN);

        Specification<Articles> spec;
        if (hasWriterRole) {
            // WRITER can only see their own articles
            spec = (root, query, cb) -> {
                Predicate predicate = cb.equal(root.get("author").get("id"), currentUser.getId());
                return predicate;
            };
        } else if (hasAdminRole) {
            // ADMIN can see all articles
            spec = (root, query, cb) -> cb.conjunction();
        } else {
            throw new HttpForbiden("Only WRITER or ADMIN can view articles");
        }

        return articlesRepository.findAll(spec, pageable)
                .map(ArticlesResponseDTO::new);
    }

    @Override
    public ArticlesResponseDTO getMyArticleById(Long id, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));

        Articles article = articlesRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Article not found with id: " + id));

        // Check if user has WRITER or ADMIN role
        Set<Roles> userRoles = currentUser.getRoles();
        boolean hasWriterRole = userRoles.stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_WRITER);
        boolean hasAdminRole = userRoles.stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN);

        if (hasWriterRole) {
            // WRITER can only view their own articles
            if (!article.getAuthor().getId().equals(currentUser.getId())) {
                throw new HttpForbiden("You can only view your own articles");
            }
        } else if (!hasAdminRole) {
            throw new HttpForbiden("Only WRITER or ADMIN can view articles");
        }
        // ADMIN can view any article

        return new ArticlesResponseDTO(article);
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

    private Notifications createNotification(Articles newArticle) {
        Notifications notification = new Notifications();

        notification.setSender(newArticle.getAuthor()); // WRITER
        notification.setReceiver(null); // broadcast cho ADMIN
        notification.setType(TypeNotification.NEW_ARTICLE);
        notification.setStatus(false);

        notification.setMessage(
                "Bài viết mới cần duyệt: " + newArticle.getTitle()
        );

        notification.setActionLink(
                "/admin/articles/" + newArticle.getId()
        );
        return notificationRepository.save(notification);
    }


}

