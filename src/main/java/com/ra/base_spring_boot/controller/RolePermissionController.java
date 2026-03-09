package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.model.constants.RolePermissionStatus;
import com.ra.base_spring_boot.model.constants.StatusUser;
import com.ra.base_spring_boot.model.entity.RolePermission;
import com.ra.base_spring_boot.services.IRolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/role-permissions")
public class RolePermissionController {
    private final IRolePermissionService rolePermissionService;

    @GetMapping
    public ResponseEntity<?> getRolePermissions(
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "sort", defaultValue = "createdDate") String sort,
            @RequestParam(value = "direction", defaultValue = "desc") String direction,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status
    ) {
        return ResponseEntity.ok(ResponseWrapper.builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(rolePermissionService.getRolePermissions(keyword, status, page, size, sort, direction))
                .build());
    }

    @PostMapping("/approve")
    public ResponseEntity<?> approvePermission(@RequestBody RolePermission rolePermission) {
        rolePermissionService.approvePermission(rolePermission);
        return ResponseEntity.status(201).body(ResponseWrapper.builder()
                .status(HttpStatus.CREATED)
                .code(201)
                .build());
    }

    @PostMapping("/reject")
    public ResponseEntity<?> rejectPermission(@RequestBody RolePermission rolePermission) {
        rolePermissionService.rejectPermission(rolePermission);
        return ResponseEntity.status(201).body(ResponseWrapper.builder()
                .status(HttpStatus.CREATED)
                .code(201)
                .build());
    }
}
