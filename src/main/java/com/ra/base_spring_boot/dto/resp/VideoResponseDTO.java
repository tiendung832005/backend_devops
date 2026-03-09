package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.entity.Articles;
import com.ra.base_spring_boot.model.entity.ArticlesVideo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoResponseDTO {
    private Long id;
    private String title;
    private String slug;
    private String thumbnailUrl;
    private String content;
    private Long authorId;
    private String authorName;
    private String status;
    private String type;
    private Integer viewCount;
    private Integer highlightLevel;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String videoUrl;
    private String description;
    private Integer duration;

    public VideoResponseDTO(Articles article, ArticlesVideo articlesVideo) {
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
            if (article.getStatus() != null) {
                this.status = article.getStatus().name();
            }
            if (article.getType() != null) {
                this.type = article.getType().name();
            }
            this.viewCount = article.getViewCount();
            this.highlightLevel = article.getHighlightLevel();
            this.publishedAt = article.getPublishedAt();
            this.createdAt = article.getCreatedAt();
            this.updatedAt = article.getUpdatedAt();
        }
        if (articlesVideo != null) {
            this.videoUrl = articlesVideo.getVideoUrl();
            this.description = articlesVideo.getDescription();
            this.duration = articlesVideo.getDuration();
        }
    }
}

