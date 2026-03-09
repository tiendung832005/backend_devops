package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.entity.Articles;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticlesResponseDTO {
    private Long id;
    private String title;
    private String slug;
    private String thumbnailUrl;
    private String content;
    private Long authorId;
    private String authorName;
    private Long categoryId;
    private String categoryName;
    private String status;
    private String type;
    private Integer viewCount;
    private Integer highlightLevel;
    private String tags;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ArticlesResponseDTO(Articles article) {
        if (article != null) {
            this.id = article.getId();
            this.title = article.getTitle();
            this.slug = article.getSlug();
            this.thumbnailUrl = article.getThumbnailUrl();
            this.content = article.getContent();
            if (article.getAuthor() != null) {
                this.authorId = article.getAuthor().getId();
                this.authorName = article.getAuthor().getFullName();
            }
            if (article.getCategory() != null) {
                this.categoryId = article.getCategory().getId();
                this.categoryName = article.getCategory().getName();
            }
            if (article.getStatus() != null) {
                this.status = article.getStatus().name();
            }
            if (article.getType() != null) {
                this.type = article.getType().name();
            }
            this.viewCount = article.getViewCount();
            this.highlightLevel = article.getHighlightLevel();
            this.tags = article.getTags();
            this.publishedAt = article.getPublishedAt();
            this.createdAt = article.getCreatedAt();
            this.updatedAt = article.getUpdatedAt();
        }
    }
}

