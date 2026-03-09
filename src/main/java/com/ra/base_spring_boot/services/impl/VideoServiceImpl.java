package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.VideoRequestDTO;
import com.ra.base_spring_boot.dto.req.VideoSearchDTO;
import com.ra.base_spring_boot.dto.resp.VideoResponseDTO;
import com.ra.base_spring_boot.exception.HttpConflict;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.constants.ArticlesStatus;
import com.ra.base_spring_boot.model.constants.ArticlesType;
import com.ra.base_spring_boot.model.entity.Articles;
import com.ra.base_spring_boot.model.entity.ArticlesVideo;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.IArticlesRepository;
import com.ra.base_spring_boot.repository.IArticlesVideoRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements IVideoService {
    private final IArticlesRepository articlesRepository;
    private final IArticlesVideoRepository articlesVideoRepository;
    private final IUserRepository userRepository;

    @Override
    public Page<VideoResponseDTO> findAll(VideoSearchDTO searchDTO, Pageable pageable) {
        Specification<Articles> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            // Filter by VIDEO type
            predicate = cb.and(predicate, cb.equal(root.get("type"), ArticlesType.VIDEO));

            if (searchDTO != null) {
                if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
                    String keyword = "%" + searchDTO.getKeyword().toLowerCase() + "%";
                    Predicate keywordPredicate = cb.or(
                            cb.like(cb.lower(root.get("title")), keyword),
                            cb.like(cb.lower(root.get("content")), keyword));
                    predicate = cb.and(predicate, keywordPredicate);
                }

                if (searchDTO.getStatus() != null && !searchDTO.getStatus().isEmpty()) {
                    try {
                        ArticlesStatus status = ArticlesStatus.valueOf(searchDTO.getStatus().toUpperCase());
                        predicate = cb.and(predicate, cb.equal(root.get("status"), status));
                    } catch (IllegalArgumentException e) {
                        // Ignore invalid status
                    }
                }

                if (searchDTO.getType() != null && !searchDTO.getType().isEmpty()) {
                    try {
                        ArticlesType type = ArticlesType.valueOf(searchDTO.getType().toUpperCase());
                        predicate = cb.and(predicate, cb.equal(root.get("type"), type));
                    } catch (IllegalArgumentException e) {
                        // Ignore invalid type
                    }
                }

                if (searchDTO.getAuthorId() != null) {
                    predicate = cb.and(predicate, cb.equal(root.get("author").get("id"), searchDTO.getAuthorId()));
                }

                if (searchDTO.getCategoryId() != null) {
                    predicate = cb.and(predicate, cb.equal(root.get("category").get("id"), searchDTO.getCategoryId()));
                }
            }

            return predicate;
        };

        return articlesRepository.findAll(spec, pageable)
                .map(article -> {
                    ArticlesVideo articlesVideo = articlesVideoRepository.findById(article.getId())
                            .orElse(null);
                    return new VideoResponseDTO(article, articlesVideo);
                });
    }

    @Override
    public VideoResponseDTO findById(Long id) {
        Articles article = articlesRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Video not found with id: " + id));

        if (article.getType() != ArticlesType.VIDEO) {
            throw new HttpNotFound("Video not found with id: " + id);
        }

        ArticlesVideo articlesVideo = articlesVideoRepository.findById(id)
                .orElse(null);

        return new VideoResponseDTO(article, articlesVideo);
    }

    @Override
    @Transactional
    public VideoResponseDTO create(VideoRequestDTO videoRequestDTO) {
        // Check if slug already exists (if provided)
        if (videoRequestDTO.getSlug() != null && !videoRequestDTO.getSlug().isEmpty()) {
            articlesRepository.findAll().stream()
                    .filter(a -> videoRequestDTO.getSlug().equals(a.getSlug()))
                    .findFirst()
                    .ifPresent(a -> {
                        throw new HttpConflict("Slug already exists");
                    });
        }

        // Get author
        User author = userRepository.findById(videoRequestDTO.getAuthorId())
                .orElseThrow(() -> new HttpNotFound("Author not found with id: " + videoRequestDTO.getAuthorId()));

        // Parse status
        ArticlesStatus status = ArticlesStatus.PENDING;
        if (videoRequestDTO.getStatus() != null && !videoRequestDTO.getStatus().isEmpty()) {
            try {
                status = ArticlesStatus.valueOf(videoRequestDTO.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                status = ArticlesStatus.PENDING;
            }
        }

        // Generate slug if not provided
        String slug = videoRequestDTO.getSlug();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug(videoRequestDTO.getTitle());
        }

        // Create article
        Articles article = new Articles();
        article.setTitle(videoRequestDTO.getTitle());
        article.setSlug(slug);
        article.setThumbnailUrl(videoRequestDTO.getThumbnailUrl());
        article.setContent(videoRequestDTO.getContent());
        article.setAuthor(author);
        article.setStatus(status);
        article.setType(ArticlesType.VIDEO);
        article.setViewCount(0);
        article.setHighlightLevel(0);
        if (status == ArticlesStatus.APPROVED) {
            article.setPublishedAt(LocalDateTime.now());
        }

        article = articlesRepository.save(article);

        // Create video - với @MapsId, chỉ cần set article, id sẽ tự động map
        ArticlesVideo articlesVideo = new ArticlesVideo();
        articlesVideo.setArticle(article); // @MapsId sẽ tự động set id từ article.getId()
        articlesVideo.setVideoUrl(videoRequestDTO.getVideoUrl());
        articlesVideo.setDescription(videoRequestDTO.getDescription());
        articlesVideo.setDuration(videoRequestDTO.getDuration());

        articlesVideo = articlesVideoRepository.save(articlesVideo);

        return new VideoResponseDTO(article, articlesVideo);
    }

    @Override
    @Transactional
    public VideoResponseDTO update(Long id, VideoRequestDTO videoRequestDTO) {
        Articles article = articlesRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Video not found with id: " + id));

        if (article.getType() != ArticlesType.VIDEO) {
            throw new HttpNotFound("Video not found with id: " + id);
        }

        // Check if slug already exists (if changed)
        if (videoRequestDTO.getSlug() != null && !videoRequestDTO.getSlug().isEmpty()) {
            if (!videoRequestDTO.getSlug().equals(article.getSlug())) {
                articlesRepository.findAll().stream()
                        .filter(a -> videoRequestDTO.getSlug().equals(a.getSlug()))
                        .findFirst()
                        .ifPresent(a -> {
                            throw new HttpConflict("Slug already exists");
                        });
            }
        }

        // Get author
        User author = userRepository.findById(videoRequestDTO.getAuthorId())
                .orElseThrow(() -> new HttpNotFound("Author not found with id: " + videoRequestDTO.getAuthorId()));

        // Parse status
        if (videoRequestDTO.getStatus() != null && !videoRequestDTO.getStatus().isEmpty()) {
            try {
                ArticlesStatus status = ArticlesStatus.valueOf(videoRequestDTO.getStatus().toUpperCase());
                article.setStatus(status);
                if (status == ArticlesStatus.APPROVED && article.getPublishedAt() == null) {
                    article.setPublishedAt(LocalDateTime.now());
                }
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }

        // Update article
        article.setTitle(videoRequestDTO.getTitle());
        if (videoRequestDTO.getSlug() != null && !videoRequestDTO.getSlug().isEmpty()) {
            article.setSlug(videoRequestDTO.getSlug());
        }
        article.setThumbnailUrl(videoRequestDTO.getThumbnailUrl());
        article.setContent(videoRequestDTO.getContent());
        article.setAuthor(author);

        article = articlesRepository.save(article);

        // Update or create video
        ArticlesVideo articlesVideo = articlesVideoRepository.findById(id).orElse(null);
        if (articlesVideo == null) {
            articlesVideo = new ArticlesVideo();
            articlesVideo.setArticle(article); // @MapsId sẽ tự động set id từ article.getId()
        } else {
            // Đảm bảo article reference được cập nhật
            articlesVideo.setArticle(article);
        }
        articlesVideo.setVideoUrl(videoRequestDTO.getVideoUrl());
        articlesVideo.setDescription(videoRequestDTO.getDescription());
        articlesVideo.setDuration(videoRequestDTO.getDuration());

        articlesVideo = articlesVideoRepository.save(articlesVideo);

        return new VideoResponseDTO(article, articlesVideo);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Articles article = articlesRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Video not found with id: " + id));

        if (article.getType() != ArticlesType.VIDEO) {
            throw new HttpNotFound("Video not found with id: " + id);
        }

        // Delete video first
        articlesVideoRepository.findById(id).ifPresent(articlesVideoRepository::delete);

        // Delete article
        articlesRepository.delete(article);
    }

    @Override
    public Page<VideoResponseDTO> searchPublished(VideoSearchDTO searchDTO, Pageable pageable) {
        // Force type VIDEO (already in spec) and status APPROVED for public search
        if (searchDTO == null) {
            searchDTO = new VideoSearchDTO();
        }
        searchDTO.setStatus(ArticlesStatus.APPROVED.name());
        return findAll(searchDTO, pageable);
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
