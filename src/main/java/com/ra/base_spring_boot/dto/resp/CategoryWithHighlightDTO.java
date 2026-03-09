package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.entity.Categories;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryWithHighlightDTO {
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
    private Long totalHighlightLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CategoryWithHighlightDTO(Categories category, Long totalHighlightLevel) {
        if (category != null) {
            this.id = category.getId();
            this.name = category.getName();
            if (category.getParent() != null) {
                this.parentId = category.getParent().getId();
                this.parentName = category.getParent().getName();
            }
            this.createdAt = category.getCreatedAt();
            this.updatedAt = category.getUpdatedAt();
        }
        this.totalHighlightLevel = totalHighlightLevel != null ? totalHighlightLevel : 0L;
    }
}

