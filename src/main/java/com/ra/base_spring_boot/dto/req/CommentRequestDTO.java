package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequestDTO
{
    @NotNull(message = "ArticleId is required")
    private Long articleId;

    private Long parentId;

    @NotBlank(message = "Content is required")
    private String content;
}

