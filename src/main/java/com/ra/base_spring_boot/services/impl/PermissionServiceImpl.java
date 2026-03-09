package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.PermissionRequestDTO;
import com.ra.base_spring_boot.dto.resp.PermissionResponseDTO;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.entity.Roles;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.repository.IRoleRepository;
import com.ra.base_spring_boot.services.IPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements IPermissionService {
    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;

    @Override
    @Transactional
    public PermissionResponseDTO updatePermissions(PermissionRequestDTO permissionRequestDTO) {
        User user = userRepository.findById(permissionRequestDTO.getUserId())
                .orElseThrow(() -> new HttpNotFound("User not found with id: " + permissionRequestDTO.getUserId()));
        
        // Check if user has admin role
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == com.ra.base_spring_boot.model.constants.RoleName.ROLE_ADMIN);
        
        if (!isAdmin) {
            throw new HttpNotFound("User is not an admin");
        }
        
        // Get roles
        Set<Roles> roles = permissionRequestDTO.getRoleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new HttpNotFound("Role not found with id: " + roleId)))
                .collect(Collectors.toSet());
        
        // Ensure at least one role is ROLE_ADMIN
        boolean hasAdminRole = roles.stream()
                .anyMatch(role -> role.getRoleName() == com.ra.base_spring_boot.model.constants.RoleName.ROLE_ADMIN);
        
        if (!hasAdminRole) {
            throw new com.ra.base_spring_boot.exception.HttpBadRequest("Admin must have ROLE_ADMIN");
        }
        
        // Update roles
        user.setRoles(roles);
        user = userRepository.save(user);
        
        return new PermissionResponseDTO(user);
    }

    @Override
    public PermissionResponseDTO getPermissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HttpNotFound("User not found with id: " + userId));
        
        // Check if user has admin role
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == com.ra.base_spring_boot.model.constants.RoleName.ROLE_ADMIN);
        
        if (!isAdmin) {
            throw new HttpNotFound("User is not an admin");
        }
        
        return new PermissionResponseDTO(user);
    }
}

