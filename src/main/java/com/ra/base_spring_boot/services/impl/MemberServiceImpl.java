package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.MemberRequestDTO;
import com.ra.base_spring_boot.dto.req.MemberRoleRequestDTO;
import com.ra.base_spring_boot.dto.req.MemberUpdateDTO;
import com.ra.base_spring_boot.dto.resp.UserResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpConflict;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.constants.StatusUser;
import com.ra.base_spring_boot.model.entity.Roles;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.IRoleRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.services.IMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements IMemberService {
    
    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserResponseDTO> findAll(String keyword, Boolean status, Long roleId, Pageable pageable) {
        Page<User> users = userRepository.searchMembers(keyword, status, roleId, pageable);
        return users.map(UserResponseDTO::new);
    }

    @Override
    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Member not found with id: " + id));
        return new UserResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO create(MemberRequestDTO memberRequestDTO) {
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(memberRequestDTO.getUsername())) {
            throw new HttpConflict("Username already exists: " + memberRequestDTO.getUsername());
        }
        
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(memberRequestDTO.getEmail())) {
            throw new HttpConflict("Email already exists: " + memberRequestDTO.getEmail());
        }
        
        // Tạo user mới
        User user = User.builder()
                .fullName(memberRequestDTO.getFullName())
                .username(memberRequestDTO.getUsername())
                .email(memberRequestDTO.getEmail())
                .password(passwordEncoder.encode(memberRequestDTO.getPassword()))
                .avatarUrl(memberRequestDTO.getAvatarUrl())
                .bio(memberRequestDTO.getBio())
                .status(memberRequestDTO.getStatus() != null ? memberRequestDTO.getStatus() : StatusUser.ACTIVE)
                .roles(new HashSet<>())
                .build();
        
        user = userRepository.save(user);
        return new UserResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO update(Long id, MemberUpdateDTO memberUpdateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Member not found with id: " + id));
        
        // Cập nhật thông tin
        if (memberUpdateDTO.getFullName() != null && !memberUpdateDTO.getFullName().isEmpty()) {
            user.setFullName(memberUpdateDTO.getFullName());
        }
        
        if (memberUpdateDTO.getEmail() != null && !memberUpdateDTO.getEmail().isEmpty()) {
            // Kiểm tra email đã tồn tại bởi user khác
            if (userRepository.existsByEmail(memberUpdateDTO.getEmail()) && 
                !user.getEmail().equals(memberUpdateDTO.getEmail())) {
                throw new HttpConflict("Email already exists: " + memberUpdateDTO.getEmail());
            }
            user.setEmail(memberUpdateDTO.getEmail());
        }
        
        if (memberUpdateDTO.getPassword() != null && !memberUpdateDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(memberUpdateDTO.getPassword()));
        }
        
        if (memberUpdateDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(memberUpdateDTO.getAvatarUrl());
        }
        
        if (memberUpdateDTO.getBio() != null) {
            user.setBio(memberUpdateDTO.getBio());
        }
        
        if (memberUpdateDTO.getStatus() != null) {
            user.setStatus(memberUpdateDTO.getStatus());
        }
        
        user = userRepository.save(user);
        return new UserResponseDTO(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Member not found with id: " + id));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateRoles(Long id, MemberRoleRequestDTO memberRoleRequestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Member not found with id: " + id));
        
        // Validate và lấy các roles
        Set<Roles> roles = memberRoleRequestDTO.getRoleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new HttpNotFound("Role not found with id: " + roleId)))
                .collect(Collectors.toSet());
        
        if (roles.isEmpty()) {
            throw new HttpBadRequest("At least one role is required");
        }
        
        user.setRoles(roles);
        user = userRepository.save(user);
        return new UserResponseDTO(user);
    }
}

