package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.resp.RolePermissionResponseDTO;
import com.ra.base_spring_boot.model.constants.RolePermissionStatus;
import com.ra.base_spring_boot.model.constants.StatusUser;
import com.ra.base_spring_boot.model.entity.RolePermission;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IRolePermissionService {
    Page<RolePermissionResponseDTO> getRolePermissions(String keyword, String status, int page, int size, String sortBy, String direction);
    void approvePermission(RolePermission rolePermission);
    void rejectPermission(RolePermission rolePermission);
}
