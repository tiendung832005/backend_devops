package com.ra.base_spring_boot.dto.req;

import lombok.Data;

@Data
public class VideoSearchDTO {
    private String keyword;
    private String status;
    private String type;
    private Long authorId;
    private Long categoryId;
}
