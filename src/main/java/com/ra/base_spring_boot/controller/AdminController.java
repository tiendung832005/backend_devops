package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.AdminRequestDTO;
import com.ra.base_spring_boot.dto.req.AdminSearchDTO;
import com.ra.base_spring_boot.dto.resp.AdminResponseDTO;
import com.ra.base_spring_boot.services.IAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/admin/admins")
@RequiredArgsConstructor
public class AdminController {
    private final IAdminService adminService;

    /**
     * @apiNote Get all admins with pagination and search
     */
    @GetMapping
    public ResponseEntity<?> getAllAdmins(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) Long roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        AdminSearchDTO searchDTO = new AdminSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setStatus(status);
        searchDTO.setRoleId(roleId);

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AdminResponseDTO> admins = adminService.findAll(searchDTO, pageable);

        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(admins)
                        .build()
        );
    }

    /**
     * @apiNote Get admin by id
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable Long id) {
        AdminResponseDTO admin = adminService.findById(id);
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(admin)
                        .build()
        );
    }

    /**
     * @apiNote Create new admin
     */
    @PostMapping
    public ResponseEntity<?> createAdmin(@Valid @RequestBody AdminRequestDTO adminRequestDTO) {
        AdminResponseDTO admin = adminService.create(adminRequestDTO);
        return ResponseEntity.created(URI.create("/api/v1/admin/admins/" + admin.getId())).body(
                ResponseWrapper.builder()
                        .status(HttpStatus.CREATED)
                        .code(201)
                        .data(admin)
                        .build()
        );
    }

    /**
     * @apiNote Update admin
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long id, @Valid @RequestBody AdminRequestDTO adminRequestDTO) {
        AdminResponseDTO admin = adminService.update(id, adminRequestDTO);
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(admin)
                        .build()
        );
    }

    /**
     * @apiNote Delete admin
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id) {
        adminService.delete(id);
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data("Admin deleted successfully")
                        .build()
        );
    }
}

