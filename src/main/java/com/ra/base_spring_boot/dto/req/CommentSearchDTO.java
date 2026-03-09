package com.ra.base_spring_boot.dto.req;

import lombok.Data;

@Data
public class CommentSearchDTO {
    private String keyword;
    private Long articleId;
    private Long userId;
}


