package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VideoRequestDTO {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String slug;
    
    private String thumbnailUrl;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotNull(message = "Author ID is required")
    private Long authorId;
    
    @NotBlank(message = "Video URL is required")
    private String videoUrl;
    
    private String description;
    
    private Integer duration;
    
    private String status;
    
    private String type;
}

