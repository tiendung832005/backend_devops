package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.entity.Comments;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CommentResponseDTO {
    private Long id;
    private Long articleId;
    private String articleTitle;
    private Long userId;
    private String username;
    private String fullName;
    private String content;
    private Boolean isHidden;
    private String hiddenReason;
    private String hiddenBy;
    private LocalDateTime hiddenAt;
    private LocalDateTime createdAt;
    private List<CommentResponseDTO> replies;

    public CommentResponseDTO(Comments comment) {
        this.id = comment.getId();
        if (comment.getArticle() != null) {
            this.articleId = comment.getArticle().getId();
            this.articleTitle = comment.getArticle().getTitle();
        }
        if (comment.getUser() != null) {
            this.userId = comment.getUser().getId();
            this.username = comment.getUser().getUsername();
            this.fullName = comment.getUser().getFullName();
        }
        this.content = comment.getContent();
        this.isHidden = comment.getIsHidden();
        this.hiddenReason = comment.getHiddenReason();
        this.hiddenBy = comment.getHiddenBy();
        this.hiddenAt = comment.getHiddenAt();
        this.createdAt = comment.getCreatedAt();
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            this.replies = comment.getReplies().stream()
                    .map(CommentResponseDTO::new)
                    .collect(Collectors.toList());
        }
    }
}
