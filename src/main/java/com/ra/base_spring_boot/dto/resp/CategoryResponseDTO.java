package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.entity.Categories;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
    private List<CategoryResponseDTO> subCategories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CategoryResponseDTO(Categories category) {
        if (category != null) {
            this.id = category.getId();
            this.name = category.getName();
            if (category.getParent() != null) {
                this.parentId = category.getParent().getId();
                this.parentName = category.getParent().getName();
            }
            if (category.getSubCategory() != null && !category.getSubCategory().isEmpty()) {
                this.subCategories = category.getSubCategory().stream()
                        .map(CategoryResponseDTO::new)
                        .collect(Collectors.toList());
            }
            this.createdAt = category.getCreatedAt();
            this.updatedAt = category.getUpdatedAt();
        }
    }
}

