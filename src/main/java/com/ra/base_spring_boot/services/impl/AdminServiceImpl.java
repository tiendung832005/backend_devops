package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.AdminRequestDTO;
import com.ra.base_spring_boot.dto.req.AdminSearchDTO;
import com.ra.base_spring_boot.dto.resp.AdminResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpConflict;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.constants.StatusUser;
import com.ra.base_spring_boot.model.entity.Roles;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.repository.IRoleRepository;
import com.ra.base_spring_boot.services.IAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {
    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<AdminResponseDTO> findAll(AdminSearchDTO searchDTO, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            
            // Filter by admin roles (users with ROLE_ADMIN)
            Predicate adminRolePredicate = root.join("roles").get("roleName")
                    .in(com.ra.base_spring_boot.model.constants.RoleName.ROLE_ADMIN);
            predicate = cb.and(predicate, adminRolePredicate);
            
            if (searchDTO != null) {
                if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
                    String keyword = "%" + searchDTO.getKeyword().toLowerCase() + "%";
                    Predicate keywordPredicate = cb.or(
                            cb.like(cb.lower(root.get("fullName")), keyword),
                            cb.like(cb.lower(root.get("username")), keyword),
                            cb.like(cb.lower(root.get("email")), keyword)
                    );
                    predicate = cb.and(predicate, keywordPredicate);
                }
                
                if (searchDTO.getStatus() != null) {
                    predicate = cb.and(predicate, cb.equal(root.get("status"), searchDTO.getStatus()));
                }
                
                if (searchDTO.getRoleId() != null) {
                    Predicate rolePredicate = cb.equal(root.join("roles").get("id"), searchDTO.getRoleId());
                    predicate = cb.and(predicate, rolePredicate);
                }
            }
            
            return predicate;
        };
        
        return userRepository.findAll(spec, pageable)
                .map(AdminResponseDTO::new);
    }

    @Override
    public AdminResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Admin not found with id: " + id));
        
        // Check if user has admin role
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == com.ra.base_spring_boot.model.constants.RoleName.ROLE_ADMIN);
        
        if (!isAdmin) {
            throw new HttpNotFound("Admin not found with id: " + id);
        }
        
        return new AdminResponseDTO(user);
    }

    @Override
    @Transactional
    public AdminResponseDTO create(AdminRequestDTO adminRequestDTO) {
        // Check if username already exists
        if (userRepository.findByUsername(adminRequestDTO.getUsername()).isPresent()) {
            throw new HttpConflict("Username already exists");
        }
        
        // Check if email already exists (if provided)
        if (adminRequestDTO.getEmail() != null && !adminRequestDTO.getEmail().isEmpty()) {
            userRepository.findAll().stream()
                    .filter(u -> adminRequestDTO.getEmail().equals(u.getEmail()))
                    .findFirst()
                    .ifPresent(u -> {
                        throw new HttpConflict("Email already exists");
                    });
        }
        
        // Get roles
        Set<Roles> roles = adminRequestDTO.getRoleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new HttpNotFound("Role not found with id: " + roleId)))
                .collect(Collectors.toSet());
        
        // Ensure at least one role is ROLE_ADMIN
        boolean hasAdminRole = roles.stream()
                .anyMatch(role -> role.getRoleName() == com.ra.base_spring_boot.model.constants.RoleName.ROLE_ADMIN);
        
        if (!hasAdminRole) {
            throw new HttpBadRequest("Admin must have ROLE_ADMIN");
        }
        
        User user = User.builder()
                .fullName(adminRequestDTO.getFullName())
                .username(adminRequestDTO.getUsername())
                .email(adminRequestDTO.getEmail())
                .password(passwordEncoder.encode(adminRequestDTO.getPassword()))
                .avatarUrl(adminRequestDTO.getAvatarUrl())
                .bio(adminRequestDTO.getBio())
                .status(adminRequestDTO.getStatus() != null ? adminRequestDTO.getStatus() : StatusUser.ACTIVE)
                .roles(roles)
                .build();
        
        user = userRepository.save(user);
        return new AdminResponseDTO(user);
    }

    @Override
    @Transactional
    public AdminResponseDTO update(Long id, AdminRequestDTO adminRequestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Admin not found with id: " + id));
        
        // Check if user has admin role
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == com.ra.base_spring_boot.model.constants.RoleName.ROLE_ADMIN);
        
        if (!isAdmin) {
            throw new HttpNotFound("Admin not found with id: " + id);
        }
        
        // Check if username already exists (if changed)
        if (!user.getUsername().equals(adminRequestDTO.getUsername())) {
            if (userRepository.findByUsername(adminRequestDTO.getUsername()).isPresent()) {
                throw new HttpConflict("Username already exists");
            }
        }
        
        // Check if email already exists (if changed)
        if (adminRequestDTO.getEmail() != null && !adminRequestDTO.getEmail().isEmpty()) {
            if (!adminRequestDTO.getEmail().equals(user.getEmail())) {
                userRepository.findAll().stream()
                        .filter(u -> adminRequestDTO.getEmail().equals(u.getEmail()))
                        .findFirst()
                        .ifPresent(u -> {
                            throw new HttpConflict("Email already exists");
                        });
            }
        }
        
        // Get roles
        Set<Roles> roles = adminRequestDTO.getRoleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new HttpNotFound("Role not found with id: " + roleId)))
                .collect(Collectors.toSet());
        
        // Ensure at least one role is ROLE_ADMIN
        boolean hasAdminRole = roles.stream()
                .anyMatch(role -> role.getRoleName() == com.ra.base_spring_boot.model.constants.RoleName.ROLE_ADMIN);
        
        if (!hasAdminRole) {
            throw new HttpBadRequest("Admin must have ROLE_ADMIN");
        }
        
        // Update user
        user.setFullName(adminRequestDTO.getFullName());
        user.setUsername(adminRequestDTO.getUsername());
        if (adminRequestDTO.getEmail() != null) {
            user.setEmail(adminRequestDTO.getEmail());
        }
        if (adminRequestDTO.getPassword() != null && !adminRequestDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(adminRequestDTO.getPassword()));
        }
        user.setAvatarUrl(adminRequestDTO.getAvatarUrl());
        user.setBio(adminRequestDTO.getBio());
        if (adminRequestDTO.getStatus() != null) {
            user.setStatus(adminRequestDTO.getStatus());
        }
        user.setRoles(roles);
        
        user = userRepository.save(user);
        return new AdminResponseDTO(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Admin not found with id: " + id));
        
        // Check if user has admin role
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == com.ra.base_spring_boot.model.constants.RoleName.ROLE_ADMIN);
        
        if (!isAdmin) {
            throw new HttpNotFound("Admin not found with id: " + id);
        }
        
        userRepository.delete(user);
    }
}

