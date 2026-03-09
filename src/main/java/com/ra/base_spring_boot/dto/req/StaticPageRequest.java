package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StaticPageRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String slug;

    private String content;

    private String pageType;

    private Boolean isActive = Boolean.TRUE;
}

