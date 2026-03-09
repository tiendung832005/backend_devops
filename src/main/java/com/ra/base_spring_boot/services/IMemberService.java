package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.MemberRequestDTO;
import com.ra.base_spring_boot.dto.req.MemberRoleRequestDTO;
import com.ra.base_spring_boot.dto.req.MemberUpdateDTO;
import com.ra.base_spring_boot.dto.resp.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IMemberService {
    Page<UserResponseDTO> findAll(String keyword, Boolean status, Long roleId, Pageable pageable);
    
    UserResponseDTO findById(Long id);
    
    UserResponseDTO create(MemberRequestDTO memberRequestDTO);
    
    UserResponseDTO update(Long id, MemberUpdateDTO memberUpdateDTO);
    
    void delete(Long id);
    
    UserResponseDTO updateRoles(Long id, MemberRoleRequestDTO memberRoleRequestDTO);
}

