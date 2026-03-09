package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BannedKeywordRequestDTO {
    @NotBlank(message = "Keyword is required")
    private String keyword;
    
    private String description;
}

