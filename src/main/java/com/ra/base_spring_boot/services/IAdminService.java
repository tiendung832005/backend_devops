package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.AdminRequestDTO;
import com.ra.base_spring_boot.dto.req.AdminSearchDTO;
import com.ra.base_spring_boot.dto.resp.AdminResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAdminService {
    Page<AdminResponseDTO> findAll(AdminSearchDTO searchDTO, Pageable pageable);
    AdminResponseDTO findById(Long id);
    AdminResponseDTO create(AdminRequestDTO adminRequestDTO);
    AdminResponseDTO update(Long id, AdminRequestDTO adminRequestDTO);
    void delete(Long id);
}

