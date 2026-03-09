package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.CategoryRequestDTO;
import com.ra.base_spring_boot.dto.resp.CategoryResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICategoryService {
    Page<CategoryResponseDTO> findAll(String search, Pageable pageable);

    Page<CategoryResponseDTO> findAll(Pageable pageable);

    List<CategoryResponseDTO> findAll();

    CategoryResponseDTO findById(Long id);

    CategoryResponseDTO create(CategoryRequestDTO categoryRequestDTO);

    CategoryResponseDTO update(Long id, CategoryRequestDTO categoryRequestDTO);

    void delete(Long id);
}
