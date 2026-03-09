package com.ra.base_spring_boot.dto.req;

import lombok.Data;

@Data
public class CommentToggleHiddenRequestDTO {
    private String reason; // Lý do ẩn (bắt buộc khi admin ẩn)
}
