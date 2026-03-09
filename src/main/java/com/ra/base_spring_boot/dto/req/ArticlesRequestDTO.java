package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticlesRequestDTO {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String slug;
    
    private String thumbnailUrl;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotNull(message = "Author ID is required")
    private Long authorId;
    
    private Long categoryId;
    
    private String status;
    
    private String type;
    
    private String tags;
    
    private Integer highlightLevel;
}



