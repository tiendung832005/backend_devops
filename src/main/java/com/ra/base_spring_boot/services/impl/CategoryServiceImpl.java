package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.CategoryRequestDTO;
import com.ra.base_spring_boot.dto.resp.CategoryResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpConflict;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.entity.Categories;
import com.ra.base_spring_boot.repository.ICategoriesRepository;
import com.ra.base_spring_boot.services.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {
    private final ICategoriesRepository categoriesRepository;

    @Override
    public Page<CategoryResponseDTO> findAll(Pageable pageable) {
        return categoriesRepository.findAll(pageable)
                .map(CategoryResponseDTO::new);
    }

    @Override
    public Page<CategoryResponseDTO> findAll(String search, Pageable pageable) {
        if (search == null || search.isEmpty()) {
            return categoriesRepository.findAll(pageable).map(CategoryResponseDTO::new);
        }
        return categoriesRepository.findAllByNameContainingIgnoreCase(search, pageable)
                .map(CategoryResponseDTO::new);
    }

    @Override
    public List<CategoryResponseDTO> findAll() {
        return categoriesRepository.findAll().stream()
                .map(CategoryResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDTO findById(Long id) {
        Categories category = categoriesRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Category not found with id: " + id));
        return new CategoryResponseDTO(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO categoryRequestDTO) {
        // Check if category name already exists
        categoriesRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(categoryRequestDTO.getName()))
                .findFirst()
                .ifPresent(c -> {
                    throw new HttpConflict("Category name already exists");
                });

        Categories category = new Categories();
        category.setName(categoryRequestDTO.getName());

        // Set parent if provided
        if (categoryRequestDTO.getParentId() != null) {
            Categories parent = categoriesRepository.findById(categoryRequestDTO.getParentId())
                    .orElseThrow(() -> new HttpNotFound(
                            "Parent category not found with id: " + categoryRequestDTO.getParentId()));

            category.setParent(parent);
        }

        category = categoriesRepository.save(category);
        return new CategoryResponseDTO(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO update(Long id, CategoryRequestDTO categoryRequestDTO) {
        Categories category = categoriesRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Category not found with id: " + id));

        // Check if category name already exists (if changed)
        if (!category.getName().equalsIgnoreCase(categoryRequestDTO.getName())) {
            categoriesRepository.findAll().stream()
                    .filter(c -> !c.getId().equals(id) && c.getName().equalsIgnoreCase(categoryRequestDTO.getName()))
                    .findFirst()
                    .ifPresent(c -> {
                        throw new HttpConflict("Category name already exists");
                    });
        }

        category.setName(categoryRequestDTO.getName());

        // Update parent if provided
        if (categoryRequestDTO.getParentId() != null) {
            if (categoryRequestDTO.getParentId().equals(id)) {
                throw new HttpBadRequest("Cannot set category as its own parent");
            }

            Categories parent = categoriesRepository.findById(categoryRequestDTO.getParentId())
                    .orElseThrow(() -> new HttpNotFound(
                            "Parent category not found with id: " + categoryRequestDTO.getParentId()));

            // Check for circular reference in subcategories
            if (hasCircularReference(category, parent)) {
                throw new HttpBadRequest("Cannot set parent: would create circular reference");
            }

            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        category = categoriesRepository.save(category);
        return new CategoryResponseDTO(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Categories category = categoriesRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Category not found with id: " + id));

        // Check if category has subcategories
        if (category.getSubCategory() != null && !category.getSubCategory().isEmpty()) {
            throw new HttpBadRequest(
                    "Cannot delete category with subcategories. Please delete or move subcategories first.");
        }

        // Check if category has articles
        if (category.getArticles() != null && !category.getArticles().isEmpty()) {
            throw new HttpBadRequest(
                    "Cannot delete category because it has " + category.getArticles().size() + " articles.");
        }

        categoriesRepository.delete(category);
    }

    private boolean hasCircularReference(Categories category, Categories potentialParent) {
        Categories current = potentialParent;
        while (current != null && current.getParent() != null) {
            if (current.getParent().getId().equals(category.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}
