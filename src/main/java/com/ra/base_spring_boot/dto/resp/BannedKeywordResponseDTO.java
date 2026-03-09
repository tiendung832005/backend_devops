package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.entity.BannedKeyword;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BannedKeywordResponseDTO {
    private Long id;
    private String keyword;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public BannedKeywordResponseDTO(BannedKeyword bannedKeyword) {
        this.id = bannedKeyword.getId();
        this.keyword = bannedKeyword.getKeyword();
        this.description = bannedKeyword.getDescription();
        this.createdAt = bannedKeyword.getCreatedAt();
        this.updatedAt = bannedKeyword.getUpdatedAt();
    }
}

