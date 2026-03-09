package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.PermissionRequestDTO;
import com.ra.base_spring_boot.dto.resp.PermissionResponseDTO;
import com.ra.base_spring_boot.services.IPermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final IPermissionService permissionService;

    /**
     * @apiNote Get permissions for an admin user
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getPermissions(@PathVariable Long userId) {
        PermissionResponseDTO permissions = permissionService.getPermissions(userId);
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(permissions)
                        .build()
        );
    }

    /**
     * @apiNote Update permissions for an admin user
     */
    @PutMapping
    public ResponseEntity<?> updatePermissions(@Valid @RequestBody PermissionRequestDTO permissionRequestDTO) {
        PermissionResponseDTO permissions = permissionService.updatePermissions(permissionRequestDTO);
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(permissions)
                        .build()
        );
    }
}

