package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.resp.RolePermissionResponseDTO;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.model.constants.RolePermissionStatus;
import com.ra.base_spring_boot.model.constants.StatusUser;
import com.ra.base_spring_boot.model.entity.RolePermission;
import com.ra.base_spring_boot.model.entity.Roles;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.IRolePermissionRepository;
import com.ra.base_spring_boot.repository.IRoleRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.repository.RolePermissionSpecification;
import com.ra.base_spring_boot.services.IRolePermissionService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements IRolePermissionService {
    private final IRolePermissionRepository rolePermissionRepository;
    private final IRoleRepository roleRepository;
    private final IUserRepository userRepository;

    @Override
    public Page<RolePermissionResponseDTO> getRolePermissions(
            String keyword,
            String status,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<RolePermission> spec =
                RolePermissionSpecification.filter(keyword, status);

        Page<RolePermission> result = rolePermissionRepository.findAll(spec, pageable);

        return result.map(rp -> {
            User user = rp.getUser();

            return new RolePermissionResponseDTO(
                    rp.getId(),
                    rp.getRoleName(),
                    rp.getText(),
                    rp.getStatus(),
                    rp.getCreatedDate(),
                    user != null ? user.getId() : null,
                    user != null ? user.getUsername() : null,
                    user != null ? user.getFullName() : null
            );
        });
    }

    @Override
    public void approvePermission(RolePermission rolePermission) {
        // 1. Lấy user
        User currentUser = userRepository
                .findUserById(rolePermission.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Lấy role từ roleName
        Roles role = roleRepository
                .findByRoleName(rolePermission.getRoleName())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // 3. Add role vào Set roles hiện tại
        currentUser.getRoles().add(role);

        // 4. Lưu user
        userRepository.save(currentUser);

        // 5. Cập nhật trạng thái duyệt
        rolePermission.setStatus(RolePermissionStatus.APPROVED);
        rolePermissionRepository.save(rolePermission);
    }

    @Override
    public void rejectPermission(RolePermission rolePermission) {
        rolePermission.setStatus(RolePermissionStatus.REJECTED);
        rolePermissionRepository.save(rolePermission);
    }
}
