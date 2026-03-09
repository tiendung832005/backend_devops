package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.PermissionRequestDTO;
import com.ra.base_spring_boot.dto.resp.PermissionResponseDTO;

public interface IPermissionService {
    PermissionResponseDTO updatePermissions(PermissionRequestDTO permissionRequestDTO);
    PermissionResponseDTO getPermissions(Long userId);
}

