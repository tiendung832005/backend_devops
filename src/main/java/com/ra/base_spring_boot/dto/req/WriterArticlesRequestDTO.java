package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WriterArticlesRequestDTO {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String slug;
    
    private String thumbnailUrl;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private Long categoryId;
    
    private String type;
    
    private String tags;
}

